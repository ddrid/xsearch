package com.xsearch.controller;

import com.xsearch.service.Indexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Controller
public class IndexController {

    @Autowired
    Indexer indexer;

    @ResponseBody
    @GetMapping("/indexer")
    public Map<String, Map<Integer, List<Integer>>> indexer(@RequestParam("start") int start, @RequestParam("end") int end) {
        System.out.println("entered");
        return indexer.makeIndex(start, end);
    }

}
