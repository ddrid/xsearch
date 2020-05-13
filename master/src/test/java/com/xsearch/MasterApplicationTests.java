package com.xsearch;

import com.xsearch.service.TestConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MasterApplicationTests {

	@Autowired
	TestConsumer testConsumer;

	@Test
	void contextLoads() {
		System.out.println(testConsumer.test());
	}

}
