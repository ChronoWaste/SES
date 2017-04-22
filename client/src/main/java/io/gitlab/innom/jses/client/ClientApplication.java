package io.gitlab.innom.jses.client;

import io.gitlab.innom.jses.core.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientApplication {

    private static final Logger logger = Logger.getLogger(ClientApplication.class.getName());

    private final ManagedChannel channel;
    private final SearchingGrpc.SearchingBlockingStub blockingStub;
    private final SearchingGrpc.SearchingStub asyncStub;

    private StreamObserver<SearchSession> requestsObserver;


    public ClientApplication(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true));
    }

    ClientApplication(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = SearchingGrpc.newBlockingStub(channel);
        asyncStub = SearchingGrpc.newStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void search(String text) {
        logger.info("Searching for " + text + " ...");

        Dork dork = Dork.newBuilder()
                .setPlainText(text).build();

        SessionRequest sessionRequest = SessionRequest.newBuilder()
                .setDork(dork)
                .build();

        Object lock = new Object();

        SearchSession session;
        try {
            session = blockingStub.createSession(sessionRequest);

            requestsObserver = asyncStub.getResults(new StreamObserver<SearchResponse>() {
                @Override
                public void onNext(SearchResponse response) {
                    response.getResultsList().forEach(result -> System.out.println(result.getTitle()));
                    requestsObserver.onNext(session);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {
                    requestsObserver.onCompleted();
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            requestsObserver.onNext(session);

            synchronized (lock) {
                lock.wait();
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws InterruptedException {
        ClientApplication client = new ClientApplication("localhost", 50051);

        try {
            /* Access a service running on the local machine on port 50051 */
            String text = "Hi!";
            if (args.length > 0) {
                text = args[0]; /* Use the arg as the name to greet if provided */
            }
            client.search(text);
        } finally {
            client.shutdown();
        }
    }

}
