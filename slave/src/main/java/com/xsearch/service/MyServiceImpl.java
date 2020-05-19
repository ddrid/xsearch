package com.xsearch.service;

import com.xsearch.testproto.lib.HelloReply;
import com.xsearch.testproto.lib.HelloRequest;
import com.xsearch.testproto.lib.MyServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hellooooooooooo" + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
