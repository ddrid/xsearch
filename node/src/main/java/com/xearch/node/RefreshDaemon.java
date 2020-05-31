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
import com.xearch.node.util.MongoUtil;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 守护进程
 * 定时进行新文档的提交和合并
 */
public class RefreshDaemon {

    private MongoDatabase db = MongoUtil.getConnection();
    private static Logger logger = Logger.getLogger(RefreshDaemon.class);

    public void start() throws Exception {
        // TODO 修改定时执行 commit 和 merge 的方式
        while (true) {
            for (int i = 0; i < 5; i++) {
                logger.info("Committing...");
                commit();
                Thread.sleep(5000);
            }
            logger.info("Merging...");
            merge();
            Thread.sleep(5000);
        }
    }

    /**
     * 将新增的文档生成段，并且为其构建倒排索引
     * 执行该方法后表示距前一次 commit 至今新提交的文档已可被搜索
     */
    public void commit() {
        long startTime = System.currentTimeMillis();

        //统计当前存在多少 segment，提供给新 segment 编号
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();
        MongoCursor<Document> iterator = allIndex.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        MongoCollection<Document> collection = db.getCollection("article");

        Bson filter = Filters.eq("segment", -1);
        FindIterable<Document> documents = collection.find(filter);
        List<Article> articles = new ArrayList<>();
        List<String> idList = new ArrayList<>();

        for (Document document : documents) {
            articles.add(JSON.parseObject(JSON.toJSONString(document), Article.class));
            idList.add(document.getString("id"));
        }

        if (articles.size() == 0) {
            logger.info("Find nothing to commit");
            return;
        }

        // 将找到 segment 为 -1 的文档改为统计出的 count 值，表示此时这些新增文档已成段落盘
        collection.updateMany(Filters.in("id", idList), new Document("$set", new Document("segment", count)));


        // 为新增文档生成倒排索引并保存
        Map<String, Map<String, List<Integer>>> invertedIndex = makeIndex(articles);
        logger.info("articleCount: " + articles.size());
        db.getCollection("invertedIndex").insertOne(new Document("segment", count)
                .append("articleCount", articles.size())
                .append("invertedIndex", invertedIndex));
        logger.info("Successfully add segment No." + count + "!");

        long endTime = System.currentTimeMillis();
        logger.info("Time: " + (double) (endTime - startTime) / 1000 + " 秒");

    }

    /**
     * 将段对应的倒排索引合并，目的是提高搜索效率
     * 分段概念参考 Lucene
     * https://www.jianshu.com/p/4d33705f37e5
     * 本实现简单将所有现存段合为一段
     */
    public void merge() {
        long startTime = System.currentTimeMillis();

        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();

        MongoCursor<Document> iterator = allIndex.iterator();

        int count = 0;
        int articleCount = 0;
        // 被合并的 segment 的 id 集合，用于合并之后将原索引删除
        List<Integer> segmentIdToMerge = new ArrayList<>();

        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<String, List<Integer>>> mergedIndex = null;
        while (iterator.hasNext()) {
            Document next = iterator.next();
            int curArticleCount = next.getInteger("articleCount");
            segmentIdToMerge.add(next.getInteger("segment"));
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
            logger.info("Find nothing to merge");
            return;
        }
        db.getCollection("article").updateMany(Filters.in("segment", segmentIdToMerge)
                , new Document("$set", new Document("segment", 0)));
        db.getCollection("invertedIndex").deleteMany(Filters.in("segment", segmentIdToMerge));
        db.getCollection("invertedIndex").insertOne(new Document("segment", 0)
                .append("articleCount", articleCount).append("invertedIndex", mergedIndex));
        logger.info("Merge successfully!");

        long endTime = System.currentTimeMillis();
        logger.info("Time: " + (double) (endTime - startTime) / 1000 + " 秒");

    }


    /**
     * 生成倒排索引
     * 结构为 word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
     * tf 表示出现几次，之后跟着 tf 个整数，分别表示该 word 在文中的偏移量，目的是高亮显示时快速定位关键词
     */
    public Map<String, Map<String, List<Integer>>> makeIndex(List<Article> articles) {

        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<String, List<Integer>>> invertedIndex = new HashMap<>();

        for (Article article : articles) {
            String id = article.getId();
            logger.info("Handling article: " + article.getTitle());
            String content = article.getContent();
            List<Term> terms = CoreStopWordDictionary.apply(IndexTokenizer.segment(content));

            // 记录总词数
            MongoCollection<Document> collection = db.getCollection("article");
            Bson filter = Filters.eq("id", article.getId());
            Document document = new Document("$set", new Document("wordCount", terms.size()));
            collection.updateOne(filter, document);


            for (Term term : terms) {
                String regex = ".*[<>,.&]+.*";
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

        return invertedIndex;
    }

}
