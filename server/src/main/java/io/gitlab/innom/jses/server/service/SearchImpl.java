package io.gitlab.innom.jses.server.service;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.gitlab.innom.jses.core.SearchGrpc;
import io.gitlab.innom.jses.core.SearchResponse;
import io.gitlab.innom.jses.core.SearchSession;
import io.gitlab.innom.jses.core.SessionRequest;
import io.gitlab.innom.jses.server.search.SearchEngine;
import io.gitlab.innom.jses.server.search.UnknownSearchEngineException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class SearchImpl extends SearchGrpc.SearchImplBase {

    private static final Logger logger = Logger.getLogger(SearchImpl.class.getName());

    private int count = 0;
    private Injector injector;


    @Inject
    public SearchImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void createSession(SessionRequest request, StreamObserver<SearchSession> responseObserver) {
        SearchSession session = SearchSession.newBuilder()
                .setDork(request.getDork())
                .setEngine(request.getEngine())
                .setStatus(SearchSession.Status.IN_PROGRESS)
                .build();

        responseObserver.onNext(session);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<SearchSession> getResults(StreamObserver<SearchResponse> responseObserver) {
        return new StreamObserver<SearchSession>() {
            @Override
            public void onNext(SearchSession session) {
                SearchEngine engine;

                try {
                    engine = injector.getInstance(Key.get(SearchEngine.class, Names.named(session.getEngine().name())));
                } catch (ConfigurationException ex){
                    ex.printStackTrace();

                    UnknownSearchEngineException searchEngineException = new UnknownSearchEngineException(session.getEngine());

                    logger.warning(searchEngineException.getMessage());
                    responseObserver.onError(searchEngineException);

                    return;
                }

                SearchResponse response = null;
                try {
                    response = engine.getResults(session);
                } catch (IOException e) {
                    responseObserver.onError(Status.ABORTED
                            .withCause(e)
                            .asException());

                    e.printStackTrace();
                }

                responseObserver.onNext(response);
                if (count > 200) {
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void onError(Throwable t) {
                // Todo: error handling
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

}
