package com.xsearch.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("slave")
public interface TestConsumer {
    @GetMapping("/teststh")
    List<Integer> test();
}
