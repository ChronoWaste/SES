package io.gitlab.innom.jses.server.search.engine;

import io.gitlab.innom.jses.core.Dork;
import io.gitlab.innom.jses.core.SearchResponse;
import io.gitlab.innom.jses.core.SearchSession;
import io.gitlab.innom.jses.server.search.SearchEngine;
import io.gitlab.innom.jses.server.search.service.BingService;
import io.gitlab.innom.jses.server.search.service.GoogleService;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import retrofit2.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class GoogleEngine implements SearchEngine {

    private static final Pattern RESULT_PATTERN = Pattern.compile("(?=http|https).*(?=&sa)");

    private GoogleService service;


    @Inject
    public GoogleEngine(GoogleService service) {
        this.service = service;
    }


    @Override
    public SearchResponse getResults(SearchSession searchSession) throws IOException {
        String query = getQuery(searchSession.getDork());

        Response<Document> response = service.listResults(query, searchSession.getStart())
                .execute();

        Document document = response
                .body();

        List<SearchResponse.SearchResult> results = new ArrayList<>();
        SearchSession.Status status = SearchSession.Status.DONE;
        Integer start = searchSession.getStart();

        if (!response.isSuccessful() && response.errorBody().string().contains("unusual traffic")) {
            status = SearchSession.Status.CAPTCHA_PROMPT;
        }

        Elements nextLinks = document != null ? document.select("#foot td.b a.fl") : null;
        if (nextLinks != null && nextLinks.size() > 0) {
            HttpUrl url = HttpUrl.parse(GoogleService.BASE_URL + nextLinks.last().attr("href"));

            start = Integer.valueOf(url.queryParameter("start"));

            if (start < searchSession.getStart()) {
                start = searchSession.getStart();
            }

            if (searchSession.getStart() != start) {
                status = SearchSession.Status.IN_PROGRESS;

                Elements resultLinks = document.select("h3.r a");

                for (Element link : resultLinks) {
                    Matcher matcher = RESULT_PATTERN.matcher(link.attr("href"));

                    if (matcher.find()) {
                        SearchResponse.SearchResult searchResult = SearchResponse.SearchResult.newBuilder()
                                .setTitle(link.text())
                                .setLink(matcher.group(0))
                                .build();

                        results.add(searchResult);
                    }
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