package io.gitlab.innom.jses.server.search;

import io.gitlab.innom.jses.core.SearchEngine;

public class UnknownSearchEngineException extends Exception {

    private io.gitlab.innom.jses.core.SearchEngine searchEngine;


    public UnknownSearchEngineException(SearchEngine searchEngine) {
        super(String.format("Unknown search engine '%s'", searchEngine.name()));
        this.searchEngine = searchEngine;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

}
