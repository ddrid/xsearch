package com.xsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MasterApplication {


	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(MasterApplication.class, args);

	}



}
