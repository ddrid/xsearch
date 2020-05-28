package com.xsearch.api.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
public class AsyncTest {
    @Async
    public Future<String> asyncSleep()  {
        try {
            System.out.println("start sleeping");
            Thread.sleep(5000);
            System.out.println("wake up");
        }catch (Exception e){
            e.printStackTrace();
        }
        return new AsyncResult<>("finished");
    }
}
