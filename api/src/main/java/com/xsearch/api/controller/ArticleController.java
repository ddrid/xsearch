package com.xsearch.api.controller;

import com.xsearch.api.entity.Article;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Controller
public class ArticleController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${nodeNum}")
    int nodeNum;

    @PutMapping("/article")
    public void putArticle(@RequestBody Article article) {

        String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001 + id.hashCode() % nodeNum).usePlaintext().build();
        ArticleServiceGrpc.ArticleServiceBlockingStub stub = ArticleServiceGrpc.newBlockingStub(channel);

        PutArticleRequest req = PutArticleRequest.newBuilder()
                .setId(id)
                .setSegment(-1)
                .setTitle(article.getTitle())
                .setUpdateTime(article.getUpdateTime())
                .setContent(article.getContent())
                .setUrl(article.getUrl()).build();

        PutArticleReply reply = stub.putArticle(req);
        System.out.println(reply.getMessage());
    }


}
