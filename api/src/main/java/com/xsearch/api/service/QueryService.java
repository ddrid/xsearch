package com.xsearch.api.service;

import com.xsearch.api.entity.Article;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.GetArticleReply;
import com.xsearch.article.lib.GetArticleRequest;
import com.xsearch.query.lib.QueryReply;
import com.xsearch.query.lib.QueryRequest;
import com.xsearch.query.lib.QueryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class QueryService {

    @Value("${nodeNum}")
    int nodeNum;

    /**
     * 向节点发出异步 rpc 请求
     *
     * @param nodeId     节点号
     * @param resultSize 结果集大小
     * @param termList   分词后词项列表
     * @return Future<QueryReply>
     */
    @Async
    public Future<QueryReply> asyncRequest(int nodeId, int resultSize, List<String> termList) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1000 + nodeId).usePlaintext().build();
        QueryServiceGrpc.QueryServiceBlockingStub stub = QueryServiceGrpc.newBlockingStub(channel);
        QueryRequest req = QueryRequest.newBuilder()
                .setResultSize(resultSize).addAllTerm(termList).build();
        return new AsyncResult<>(stub.query(req));
    }

    /**
     * 合并查询结果
     *
     * @param replyList  查询结果列表
     * @param resultSize 结果集大小
     * @return List<Article>
     */
    public List<Article> mergeResult(List<QueryReply> replyList, int resultSize) {
        List<String> idList = replyList.get(0).getIdList();
        List<Double> scoreList = replyList.get(0).getScoreList();
        List<Integer> offsetList = replyList.get(0).getOffsetList();
        for (int j = 1; j < nodeNum; j++) {
            List<String> mergedIdList = new ArrayList<>();
            List<Double> mergedScoreList = new ArrayList<>();
            List<Integer> mergedOffsetList = new ArrayList<>();
            List<String> nextIdList = replyList.get(j).getIdList();
            List<Double> nextScoreList = replyList.get(j).getScoreList();
            List<Integer> nextOffsetList = replyList.get(j).getOffsetList();
            for (int i = 0, m = 0, n = 0; i < resultSize; i++) {
                double pre = scoreList.size() == m ? -1 : scoreList.get(m);
                double cur = nextScoreList.size() == n ? -1 : nextScoreList.get(n);
                if (pre == -1 && cur == -1) {
                    break;
                }
                if (pre > cur) {
                    mergedIdList.add(idList.get(m));
                    mergedScoreList.add(scoreList.get(m));
                    mergedOffsetList.add(offsetList.get(m));
                    m++;
                } else {
                    mergedIdList.add(nextIdList.get(n));
                    mergedScoreList.add(nextScoreList.get(n));
                    mergedOffsetList.add(nextOffsetList.get(n));
                    n++;
                }
            }
            idList = mergedIdList;
            scoreList = mergedScoreList;
            offsetList = mergedOffsetList;
        }
        System.out.println();
        System.out.println("merged:");
        System.out.println(idList);
        System.out.println(scoreList);
        System.out.println(offsetList);

        List<Article> resList = new ArrayList<>();

        for (int i = 0; i < idList.size(); i++) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001 + Math.abs(idList.get(i).hashCode()) % nodeNum).usePlaintext().build();
            ArticleServiceGrpc.ArticleServiceBlockingStub stub = ArticleServiceGrpc.newBlockingStub(channel);

            GetArticleRequest req = GetArticleRequest.newBuilder()
                    .setId(idList.get(i)).build();

            GetArticleReply reply = stub.getArticle(req);

            Article article = new Article(reply.getId(), reply.getSegment(),
                    reply.getUpdateTime(), reply.getUrl(),
                    reply.getTitle(), reply.getContent(),
                    scoreList.get(i), offsetList.get(i));

            resList.add(article);
        }

        return resList;
    }
}
