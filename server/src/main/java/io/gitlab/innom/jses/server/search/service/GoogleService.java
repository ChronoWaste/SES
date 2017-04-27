package io.gitlab.innom.jses.server.search.service;

import org.jsoup.nodes.Document;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleService {

    String BASE_URL = "https://www.google.com";


    @GET("/search")
    Call<Document> listResults(@Query("q") String query, @Query("start") Integer start);


}
