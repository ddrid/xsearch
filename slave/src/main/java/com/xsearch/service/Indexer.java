package com.xsearch.service;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.xsearch.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class Indexer {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Map<Integer, List<Integer>>> makeIndex(int start, int end) {

        long startTime = System.currentTimeMillis();

        // word1 -> {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
        Map<String, Map<Integer, List<Integer>>> index = new HashMap<>();

        for (int i = start; i < end; i++) {
            System.out.println(i);
            Query query = new Query(Criteria.where("index").is(i));
            Article article = mongoTemplate.findOne(query, Article.class);
            assert article != null;
            String content = article.getContent();
            List<Term> segment = IndexTokenizer.segment(content);
            for (Term term : segment) {
                String regex = ".*[<>,.]+.*";
                //去除html标签以及特殊符号
                if (!Pattern.compile(regex).matcher(term.word).matches()) {
                    // 不存在该词项
                    index.computeIfAbsent(term.word, k -> new HashMap<>());

                    //{id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
                    Map<Integer, List<Integer>> indexMap = index.get(term.word);

                    //词项内不存在这篇文章的id
                    if (indexMap.get(i) == null) {
                        List<Integer> indexList = new ArrayList<>();
                        //先将tf置0
                        indexList.add(0);
                        indexMap.put(i, indexList);
                    }

                    List<Integer> indexList = indexMap.get(i);

                    // 将tf值加一
                    indexList.set(0, indexList.get(0) + 1);

                    // 将该词在文中的offset追加至list尾部
                    indexList.add(term.offset);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + (double) (endTime - startTime) / 1000 + " 秒");
        return index;
    }
}
