package com.xsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients
@SpringBootApplication
public class SlaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlaveApplication.class, args);
	}

}
