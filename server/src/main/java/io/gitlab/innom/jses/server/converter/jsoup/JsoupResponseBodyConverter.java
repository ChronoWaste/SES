package io.gitlab.innom.jses.server.converter.jsoup;

import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Converter;

import java.io.IOException;

public class JsoupResponseBodyConverter implements Converter<ResponseBody, Document> {

    @Override
    public Document convert(ResponseBody value) throws IOException {
        return Jsoup.parse(value.string());
    }

}
