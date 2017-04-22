package io.gitlab.innom.jses.server;

import io.gitlab.innom.jses.server.service.SearchingImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerApplication {

    private static final int PORT = 50051;

    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    private Server server;


    private void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new SearchingImpl())
                .build()
                .start();

        logger.info("ServerApplication started, listening on " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            ServerApplication.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerApplication server = new ServerApplication();
        server.start();
        server.blockUntilShutdown();
    }

}
