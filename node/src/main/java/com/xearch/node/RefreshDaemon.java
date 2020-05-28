package com.xearch.node;

import com.alibaba.fastjson.JSON;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.xearch.node.entity.Article;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RefreshDaemon {

    private MongoDatabase db = MongoUtil.getConnection();

    public void start() throws Exception {
        while (true) {
            for (int i = 0; i < 5; i++) {
                System.out.println("------committing------");
                commit();
                Thread.sleep(2000);
            }
            System.out.println("------merging------");
            merge();
            Thread.sleep(2000);
        }
    }

    /**
     * 每隔一段时间将新增的文档生成段，并且为其构建倒排索引
     */
    public void commit() {
        //找出 segment 为 -1 的文档，即在上一次 commit 至今新增的文档
        Bson filter = Filters.eq("segment", -1);
        MongoCollection<Document> collection = db.getCollection("article");
        FindIterable<Document> documents = collection.find(filter);
        List<Article> articles = new ArrayList<>();
        for (Document document : documents) {
            articles.add(JSON.parseObject(JSON.toJSONString(document), Article.class));
        }

        if (articles.size() == 0) {
            System.out.println("Find nothing to commit");
            return;
        }

        //统计当前存在多少 segment，提供给新 segment 编号
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();
        MongoCursor<Document> iterator = allIndex.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        System.out.println(count);

        // 将找到 segment 为 -1 的文档改为统计出的 count 值，表示此时这些新增文档已成段落盘
        Document document = new Document("$set", new Document("segment", count));
        collection.updateMany(filter, document);

        // 为新增文档生成倒排索引并保存
        Map<String, Map<String, List<Integer>>> invertedIndex = makeIndex(articles);
        db.getCollection("invertedIndex").insertOne(new Document("segment", count)
                .append("articleCount",articles.size())
                .append("invertedIndex", invertedIndex));
        System.out.println("Successfully add segment No." + count + "!");

    }

    /**
     * 将段对应的倒排索引合并，提高搜索效率
     */
    public void merge() {
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();

        MongoCursor<Document> iterator = allIndex.iterator();

        int count = 0;
        int articleCount = 0;
        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<String, List<Integer>>> mergedIndex = null;
        while (iterator.hasNext()) {
            Document next = iterator.next();
            int curArticleCount = next.getInteger("articleCount");
            articleCount += curArticleCount;
            Map<String, Map<String, List<Integer>>> cur = next.get("invertedIndex", Map.class);
            if (count == 0) {
                mergedIndex = cur;
                count++;
                continue;
            }
            for (String key : cur.keySet()) {
                if (mergedIndex.get(key) != null) {
                    mergedIndex.get(key).putAll(cur.get(key));
                } else {
                    mergedIndex.put(key, cur.get(key));
                }
            }
            count++;
        }
        if (count == 0 || count == 1) {
            System.out.println("Find nothing to merge");
            return;
        }
        db.getCollection("invertedIndex").drop();
        db.getCollection("invertedIndex").insertOne(new Document("segment", 0)
                .append("articleCount",articleCount).append("invertedIndex", mergedIndex));
        db.getCollection("article").updateMany(Filters.ne("segment", 0)
                , new Document("$set", new Document("segment", 0)));
        System.out.println("Merge successfully!");
    }


    /**
     * 生成倒排索引
     */
    public Map<String, Map<String, List<Integer>>> makeIndex(List<Article> articles) {

        long startTime = System.currentTimeMillis();

        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<String, List<Integer>>> invertedIndex = new HashMap<>();


        for (Article article : articles) {
            String id = article.getId();
            System.out.println("handling article No." + id);
            String content = article.getContent();
            List<Term> terms = CoreStopWordDictionary.apply(IndexTokenizer.segment(content));

            // 记录总词数
            MongoCollection<Document> collection = db.getCollection("article");
            Bson filter = Filters.eq("id", article.getId());
            Document document = new Document("$set", new Document("wordCount", terms.size()));
            collection.updateOne(filter, document);


            for (Term term : terms) {
                String regex = ".*[<>,.]+.*";
                //去除html标签以及特殊符号
                if (!Pattern.compile(regex).matcher(term.word).matches()) {
                    // 不存在该词项
                    invertedIndex.computeIfAbsent(term.word, k -> new HashMap<>());

                    //{id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
                    Map<String, List<Integer>> indexMap = invertedIndex.get(term.word);

                    //词项内不存在这篇文章的id
                    if (indexMap.get(id) == null) {
                        List<Integer> indexList = new ArrayList<>();
                        //先将tf置0
                        indexList.add(0);
                        indexMap.put(id, indexList);
                    }

                    List<Integer> indexList = indexMap.get(id);

                    // 将tf值加一
                    indexList.set(0, indexList.get(0) + 1);

                    // 将该词在文中的offset追加至list尾部
                    indexList.add(term.offset);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + (double) (endTime - startTime) / 1000 + " 秒");

        return invertedIndex;
    }

}
