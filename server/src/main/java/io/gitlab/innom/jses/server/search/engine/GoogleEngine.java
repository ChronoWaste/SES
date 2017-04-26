package io.gitlab.innom.jses.server.search.engine;

import io.gitlab.innom.jses.core.Dork;
import io.gitlab.innom.jses.core.SearchResponse;
import io.gitlab.innom.jses.core.SearchSession;
import io.gitlab.innom.jses.server.search.SearchEngine;
import io.gitlab.innom.jses.server.search.service.GoogleService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoogleEngine implements SearchEngine {

    private GoogleService service;


    @Inject
    public GoogleEngine(GoogleService service) {
        this.service = service;
    }


    @Override
    public SearchResponse getResults(SearchSession searchSession) {
        return null;
    }

}