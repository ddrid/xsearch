package com.xearch.node.service;

import com.google.protobuf.ByteString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.xearch.node.util.MongoUtil;
import com.xsearch.query.lib.QueryReply;
import com.xsearch.query.lib.QueryRequest;
import com.xsearch.query.lib.QueryServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

import java.util.*;

public class QueryServiceImpl extends QueryServiceGrpc.QueryServiceImplBase {

    /**
     * 执行本地查询
     * 使用向量空间模型根据本地的倒排索引找出 topK 个分值最高的文档
     * 向量空间模型参考 Lucene 实现
     * https://blog.csdn.net/weixin_34205826/article/details/92088479
     */
    @Override
    public void query(QueryRequest request, StreamObserver<QueryReply> responseObserver) {
        MongoDatabase db = MongoUtil.getConnection();
        FindIterable<Document> allIndex = db.getCollection("invertedIndex").find();
        MongoCursor<Document> iterator = allIndex.iterator();

        List<ByteString> byteStringList = request.getTermList().asByteStringList();

        // 最后需要返回 id 和 score 列表的长度
        int resultSize = request.getResultSize();

        List<String> termList = new ArrayList<>();

        for (ByteString bs : byteStringList) {
            termList.add(bs.toStringUtf8());
        }

        List<String> idList = new ArrayList<>();
        List<Double> scoreList = new ArrayList<>();
        List<Integer> offsetList = new ArrayList<>();

        // 对每一个段进行检索，最后进行归并
        while (iterator.hasNext()) {

            Document next = iterator.next();
            // 当前在哪一个段中检索
            int segment = next.getInteger("segment");

            // query 中每一个 term 的 idf 值
            List<Double> idfList = new ArrayList<>();

            // 当前段的倒排索引
            Map<String, Map<String, List<Integer>>> invertedIndex = next.get("invertedIndex", Map.class);

            // 当前段包含的文章总数
            int articleCount = next.getInteger("articleCount");

            // 所有包含查询项中任一 term 的文章的得分
            Map<String, Double> curScoreMap = new HashMap<>();
            // 查询结果文中包含词项位置的 offset，用于定位高亮位置
            Map<String, Integer> curOffsetMap = new HashMap<>();

            // 遍历查询中包含的 term
            for (int i = 0; i < termList.size(); i++) {
                // {id1 -> [tf1, off11, off12...], id2 -> [tf2, off21, off22...], ... }
                Map<String, List<Integer>> idMap = invertedIndex.get(termList.get(i));
                if (idMap != null) {
                    double idf = 1 + Math.log(articleCount / (idMap.keySet().size() + 1));
                    idfList.add(idf);
                    for (String id : idMap.keySet()) {
                        // 存一个含有查询词项的 offset 即可
                        curOffsetMap.putIfAbsent(id, idMap.get(id).get(1));
                        // 计算 q x d
                        curScoreMap.put(id, curScoreMap.getOrDefault(id, 0.0) + Math.sqrt(idMap.get(id).get(0)) * idf * idf);
                    }
                } else {
                    idfList.add(1 + Math.log(articleCount));
                }
            }

            double qLen = 0.0;
            for (Double idf : idfList) {
                qLen += (idf * idf);
            }
            qLen = Math.sqrt(qLen);
            for (String id : curScoreMap.keySet()) {
                FindIterable<Document> article = db.getCollection("article").find(Filters.eq("id", id)).limit(1);
                int wordCount = (int) article.cursor().next().get("wordCount");
                double dLen = Math.sqrt(wordCount);
                // q x d / (|q| x |d|)
                curScoreMap.put(id, curScoreMap.get(id) / (qLen * dLen));
            }

            List<Map.Entry<String, Double>> sortedScoreMapList = new ArrayList<>(curScoreMap.entrySet());
            Comparator<Map.Entry<String, Double>> comparator = Map.Entry.comparingByValue();
            sortedScoreMapList.sort(comparator.reversed());

            // 合并前一段结果
            if (idList.size() == 0) {
                for (int i = 0; i < resultSize; i++) {
                    if (i == sortedScoreMapList.size()) {
                        break;
                    }
                    idList.add(sortedScoreMapList.get(i).getKey());
                    scoreList.add(sortedScoreMapList.get(i).getValue());
                    offsetList.add(curOffsetMap.get(sortedScoreMapList.get(i).getKey()));
                }
            } else {
                List<String> mergedIdList = new ArrayList<>();
                List<Double> mergedScoreList = new ArrayList<>();
                List<Integer> mergedOffsetList = new ArrayList<>();
                for (int i = 0, m = 0, n = 0; i < resultSize; i++) {
                    double pre = scoreList.size() == m ? -1 : scoreList.get(m);
                    double cur = sortedScoreMapList.size() == n ? -1 : sortedScoreMapList.get(n).getValue();
                    if (pre == -1 && cur == -1) {
                        break;
                    }
                    if (pre > cur) {
                        mergedIdList.add(idList.get(m));
                        mergedScoreList.add(scoreList.get(m));
                        mergedOffsetList.add(offsetList.get(m));
                        m++;
                    } else {
                        mergedIdList.add(sortedScoreMapList.get(n).getKey());
                        mergedScoreList.add(sortedScoreMapList.get(n).getValue());
                        mergedOffsetList.add(curOffsetMap.get(sortedScoreMapList.get(n).getKey()));
                        n++;
                    }

                }
                idList = mergedIdList;
                scoreList = mergedScoreList;
                offsetList = mergedOffsetList;
            }
        }

        QueryReply reply = QueryReply.newBuilder().setMessage("success")
                .addAllId(idList).addAllScore(scoreList).addAllOffset(offsetList).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
