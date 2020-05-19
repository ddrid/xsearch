package com.xsearch.controller;

import com.xsearch.service.Spider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpiderController {
    @Autowired
    Spider spider;

    @ResponseBody
    @GetMapping("/spider")
    public void spider () {
        try {
            spider.spider();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
