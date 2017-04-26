package io.gitlab.innom.jses.server.search.service;

import org.jsoup.nodes.Document;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BingService {

    String BASE_URL = "http://www.bing.com";


    @GET("/search")
    Call<Document> listResults(@Query("q") String query, @Query("first") Integer first);

}
