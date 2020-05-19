package com.xsearch;

import com.xsearch.testproto.lib.HelloReply;
import com.xsearch.testproto.lib.HelloRequest;
import com.xsearch.testproto.lib.MyServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SlaveApplicationTests {

	@Test
	void contextLoads() {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090).usePlaintext().build();
		MyServiceGrpc.MyServiceBlockingStub myServiceBlockingStub =  MyServiceGrpc.newBlockingStub(channel);

		HelloRequest req = HelloRequest.newBuilder().setName("neo").build();
		HelloReply reply = myServiceBlockingStub.sayHello(req);
	}

}
