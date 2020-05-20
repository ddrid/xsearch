package com.xearch.node;

import com.xearch.node.service.ArticleServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.Optional;

public class NodeServer {
    private int port = 1000 + Integer.parseInt(System.getProperty("id"));
    private Server server;

    public void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new ArticleServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is shutting down");
            NodeServer.this.stop();
            System.err.println("Server shut down");
        }));

        RefreshDaemon refreshDaemon = new RefreshDaemon();
        refreshDaemon.start();
    }


    public void stop() {
        Optional.of(server).map(Server::shutdown).orElse(null);
    }

    public void blockUnitShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        NodeServer server = new NodeServer();
        server.start();
        server.blockUnitShutdown();
    }

}
