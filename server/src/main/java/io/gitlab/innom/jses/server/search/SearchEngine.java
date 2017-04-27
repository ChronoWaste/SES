package io.gitlab.innom.jses.server.search;

import io.gitlab.innom.jses.core.SearchResponse;
import io.gitlab.innom.jses.core.SearchSession;

import java.io.IOException;

public interface SearchEngine {

    SearchResponse getResults(SearchSession searchSession) throws IOException;

}
