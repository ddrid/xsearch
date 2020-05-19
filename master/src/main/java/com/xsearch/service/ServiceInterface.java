//package com.xsearch.service;
//
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Future;
//
//public interface ServiceInterface {
//    @GetMapping("/indexer")
//    Map<String, Map<Integer, List<Integer>>> makeIndex(@RequestParam("start") int start, @RequestParam("end") int end);
//
//
//    @GetMapping("/spider")
//    void spider();
//}
