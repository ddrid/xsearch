package com.xsearch.controller;


import com.xsearch.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class ArticleController {

//    @Autowired
//    private DiscoveryClient discoveryClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PutMapping("/article")
    public void putArticle (@RequestBody Article article) {

        article.setSegment(-1);
        mongoTemplate.insert(article);
    }


}
