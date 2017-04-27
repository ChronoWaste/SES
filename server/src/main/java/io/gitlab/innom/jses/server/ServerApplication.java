package io.gitlab.innom.jses.server;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.gitlab.innom.jses.server.search.SearchModule;
import io.grpc.Server;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerApplication {

    public static final int PORT = 50051;

    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    private Server server;


    @Inject
    public ServerApplication(Server server) {
        this.server = server;
    }

    private void start() throws IOException {
        server.start();

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
        Injector injector = Guice.createInjector(new SearchModule());

        ServerApplication server = injector.getInstance(ServerApplication.class);

        server.start();
        server.blockUntilShutdown();
    }

}
