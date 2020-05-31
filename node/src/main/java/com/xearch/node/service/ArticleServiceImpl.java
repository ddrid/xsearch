package com.xearch.node.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.xearch.node.entity.Article;
import com.xearch.node.util.MongoUtil;
import com.xsearch.article.lib.*;
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

    @Override
    public void getArticle(GetArticleRequest request, StreamObserver<GetArticleReply> responseObserver) {
        MongoDatabase db = MongoUtil.getConnection();
        MongoCursor<Document> cursor = db.getCollection("article").find(Filters.eq("id", request.getId())).limit(1).cursor();
        Document article = cursor.next();

        GetArticleReply reply = GetArticleReply.newBuilder()
                .setId(article.getString("id"))
                .setSegment(article.getInteger("segment"))
                .setUpdateTime(article.getInteger("updateTime"))
                .setUrl(article.getString("url"))
                .setTitle(article.getString("title"))
                .setContent(article.getString("content")).build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}

