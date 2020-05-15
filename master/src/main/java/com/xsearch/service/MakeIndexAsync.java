package com.xsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Service
public class MakeIndexAsync {
    @Autowired
    MakeIndex makeIndex;

    @Async
    public Future<Map<String, Map<Integer, List<Integer>>>> getIndexResult(int start, int end) {
        return new AsyncResult<>(makeIndex.makeIndex(start, end));
    }
}
