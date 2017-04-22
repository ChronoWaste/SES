package io.gitlab.innom.jses.server.service;

import io.gitlab.innom.jses.core.*;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;

public class SearchingImpl extends SearchingGrpc.SearchingImplBase {

    private int count = 0;

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
                List<SearchResponse.SearchResult> results = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    SearchResponse.SearchResult result = SearchResponse.SearchResult.newBuilder()
                            .setTitle("Title " + i)
                            .setDescription("Description: " + i)
                            .build();

                    results.add(result);
                    count++;
                }

                SearchResponse response = SearchResponse.newBuilder()
                        .setSession(session)
                        .addAllResults(results)
                        .build();

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
