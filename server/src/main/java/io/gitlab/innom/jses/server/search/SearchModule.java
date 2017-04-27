package io.gitlab.innom.jses.server.search;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import io.gitlab.innom.jses.core.SearchGrpc;
import io.gitlab.innom.jses.server.ServerApplication;
import io.gitlab.innom.jses.server.converter.jsoup.JsoupConverterFactory;
import io.gitlab.innom.jses.server.search.engine.BingEngine;
import io.gitlab.innom.jses.server.search.engine.GoogleEngine;
import io.gitlab.innom.jses.server.search.service.BingService;
import io.gitlab.innom.jses.server.search.service.GoogleService;
import io.gitlab.innom.jses.server.service.SearchImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class SearchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SearchGrpc.SearchImplBase.class)
                .to(SearchImpl.class);

        bind(SearchEngine.class)
                .annotatedWith(Names.named(io.gitlab.innom.jses.core.SearchEngine.GOOGLE.name()))
                .to(GoogleEngine.class);

        bind(SearchEngine.class)
                .annotatedWith(Names.named(io.gitlab.innom.jses.core.SearchEngine.BING.name()))
                .to(BingEngine.class);
    }

    @Provides
    Server provideSearchServer(SearchGrpc.SearchImplBase search) {
        return ServerBuilder.forPort(ServerApplication.PORT)
                .addService(search)
                .build();
    }

    @Provides
    @Singleton
    Retrofit.Builder provideRetrofitBuilder(JsoupConverterFactory converterFactory) {
        Proxy proxyTest = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 5555));

        OkHttpClient client = new OkHttpClient().newBuilder()
                //.proxy(proxyTest)
                .build();

        return new Retrofit.Builder()
                .client(client)
                .addConverterFactory(converterFactory);
    }

    @Provides
    @Singleton
    BingService provideBingService(Retrofit.Builder builder) {
        Retrofit retrofit = builder
                .baseUrl(BingService.BASE_URL)
                .build();

        return retrofit.create(BingService.class);
    }

    @Provides
    @Singleton
    GoogleService provideGoogleService(Retrofit.Builder builder) {
        Retrofit retrofit = builder
                .baseUrl(GoogleService.BASE_URL)
                .build();

        return retrofit.create(GoogleService.class);
    }

}
