package com.xearch.node.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.MongoDatabase;
import com.xearch.node.MongoUtil;
import com.xearch.node.entity.Article;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

public class ArticleServiceImpl extends ArticleServiceGrpc.ArticleServiceImplBase {

    @Override
    public void putArticle(PutArticleRequest request, StreamObserver<PutArticleReply> responseObserver) {
        PutArticleReply reply = PutArticleReply.newBuilder().setMessage("success").build();

        Article article = new Article(request.getId(), request.getSegment(), request.getUpdateTime()
                , request.getUrl(), request.getTitle(), request.getContent());

        MongoDatabase db = MongoUtil.getConnection();
        db.getCollection("article").insertOne(Document.parse(JSON.toJSONString(article)));

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

