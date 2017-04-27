package io.gitlab.innom.jses.server.search.engine;

import io.gitlab.innom.jses.core.Dork;
import io.gitlab.innom.jses.core.SearchResponse;
import io.gitlab.innom.jses.core.SearchSession;
import io.gitlab.innom.jses.server.search.SearchEngine;
import io.gitlab.innom.jses.server.search.service.BingService;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BingEngine implements SearchEngine {

    private BingService service;


    @Inject
    public BingEngine(BingService service) {
        this.service = service;
    }


    @Override
    public SearchResponse getResults(SearchSession searchSession) throws IOException {
        String query = getQuery(searchSession.getDork());

        Document document = service.listResults(query, searchSession.getStart())
                .execute()
                .body();

        List<SearchResponse.SearchResult> results = new ArrayList<>();
        SearchSession.Status status = SearchSession.Status.DONE;
        Integer start = searchSession.getStart();

        Elements nextLinks = document.select("#b_results li.b_pag a.sb_pagN");
        if (nextLinks.size() > 0) {
            HttpUrl url = HttpUrl.parse(BingService.BASE_URL + nextLinks.get(0).attr("href"));

            start = Integer.valueOf(url.queryParameter("first"));

            if (searchSession.getStart() != start) {
                status = SearchSession.Status.IN_PROGRESS;

                Elements resultLinks = document.select("#b_results li.b_algo h2 a");

                for (Element link : resultLinks) {
                    SearchResponse.SearchResult searchResult = SearchResponse.SearchResult.newBuilder()
                            .setTitle(link.text())
                            .setLink(link.attr("href"))
                            .build();

                    results.add(searchResult);
                }
            }
        }

        SearchSession newSession = SearchSession.newBuilder()
                .setDork(searchSession.getDork())
                .setStatus(status)
                .setEngine(searchSession.getEngine())
                .setStart(start)
                .build();

        return SearchResponse.newBuilder()
                .setSession(newSession)
                .addAllResults(results).build();
    }

    private String getQuery(Dork dork) {
        return dork.getPlainText();
    }

}
