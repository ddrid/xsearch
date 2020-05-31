package com.xsearch.api.controller;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.xsearch.api.entity.Article;
import com.xsearch.api.service.QueryService;
import com.xsearch.query.lib.QueryReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Controller
public class QueryController {
    /**
     * 节点数量
     */
    @Value("${nodeNum}")
    int nodeNum;

    @Autowired
    QueryService queryService;

    /**
     * 执行分布式查询，参考 ElasticSearch 实现
     * https://www.elastic.co/guide/cn/elasticsearch/guide/current/_query_phase.html
     *
     * @param query 查询语句
     * @param from  分页参数，表示从第几条开始
     * @param size  分页参数，表示返回列表集合长度
     * @return List<Article>
     */
    @ResponseBody
    @GetMapping("/query")
    public List<Article> query(@RequestParam("query") String query,
                               @RequestParam("from") int from,
                               @RequestParam("size") int size) throws Exception {
        // 分词
        List<Term> segment = CoreStopWordDictionary.apply(IndexTokenizer.segment(query));
        List<String> termList = new ArrayList<>();
        for (Term term : segment) {
            termList.add(term.word);
        }

        // 将查询发送到所有 node 节点进行查询，结果存放于 replyList
        List<Future<QueryReply>> futureList = new ArrayList<>();
        List<QueryReply> replyList = new ArrayList<>();
        for (int i = 0; i < nodeNum; i++) {
            futureList.add(queryService.asyncRequest(i + 1, from + size, termList));
        }

        // 轮询是否所有节点已返回结果
        boolean running = true;
        while (running) {
            running = false;
            for (Future<QueryReply> future : futureList) {
                if (!future.isDone()) {
                    running = true;
                    break;
                }
            }
            Thread.sleep(50);
        }
        for (Future<QueryReply> future : futureList) {
            replyList.add(future.get());
        }

        // 将各节点返回的内容合并
        List<Article> articles = queryService.mergeResult(replyList, from + size);
        return articles.subList(from, from + size);

    }
}
