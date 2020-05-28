package com.xsearch.api;

import com.xsearch.api.controller.AsyncTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@EnableAsync
@SpringBootTest
class ApiApplicationTests {

    @Autowired
    AsyncTest asyncTest;

    @Test
    void contextLoads() throws InterruptedException, ExecutionException {
        List<Future<String>> futureList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            futureList.add(asyncTest.asyncSleep());
        }
        boolean flag = true;
        while (flag) {
            flag = false;
            for (Future<String> future : futureList) {
                if (!future.isDone()) {
                    flag = true;
                    break;
                }
            }
            for (Future<String> future : futureList) {
                System.out.println(future.get());
            }
        }
    }

}


