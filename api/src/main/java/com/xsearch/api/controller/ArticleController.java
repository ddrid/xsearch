package com.xsearch.api.controller;

import com.xsearch.api.entity.Article;
import com.xsearch.article.lib.ArticleServiceGrpc;
import com.xsearch.article.lib.PutArticleReply;
import com.xsearch.article.lib.PutArticleRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class ArticleController {
    @Value("${nodeNum}")
    int nodeNum;

    /**
     * 将 article 转发到各 node 节点上，采用 Math.abs(id.hashCode()) % nodeNum 进行负载均衡
     *
     * @param article 用户发送的 Article 类
     */
    @ResponseBody
    @PutMapping("/article")
    public void putArticle(@RequestBody Article article) {

        // 创建随机 id
        String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        // 发送 rpc 请求
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001 + Math.abs(id.hashCode()) % nodeNum).usePlaintext().build();
        ArticleServiceGrpc.ArticleServiceBlockingStub stub = ArticleServiceGrpc.newBlockingStub(channel);

        PutArticleRequest req = PutArticleRequest.newBuilder()
                .setId(id)
                .setSegment(-1)
                .setTitle(article.getTitle())
                .setUpdateTime(article.getUpdateTime())
                .setContent(article.getContent())
                .setUrl(article.getUrl()).build();

        PutArticleReply reply = stub.putArticle(req);
        System.out.println("Transmit article to NodeServer" + (1 + Math.abs(id.hashCode()) % nodeNum) + ": " + article.getTitle());
        channel.shutdown();
    }


}
