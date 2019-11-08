package com.xuvjso.nfmovies.Utils;


import okhttp3.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    public final static int READ_TIMEOUT = 10;
    public final static int CONNECT_TIMEOUT = 10;
    public final static int WRITE_TIMEOUT = 5;
    private static OkHttpUtil mInstance;
    private OkHttpClient mOkHttpClient;

    private OkHttpUtil() {
        okhttp3.OkHttpClient.Builder ClientBuilder = new okhttp3.OkHttpClient.Builder();
        ClientBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        ClientBuilder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        ClientBuilder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        mOkHttpClient = ClientBuilder.build();
    }

    public static synchronized OkHttpUtil getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpUtil();
        }
        return mInstance;
    }

    public String getHtml(String url, String referer) {
        String html = null;
        Response response = getInstance().get(url, referer);
        if (response == null) return null;
        try {
            html = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }

    public Response get(String url, String referer) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url)
                .addHeader("Referer", referer)
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36")
                .build();
        Call call = mOkHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    public Response post(String url, Map<String, String> params, String referer) {
        RequestBody body = setRequestBody(params);
        Request.Builder builder = new Request.Builder();
        Request request = builder.post(body).url(url).addHeader("Referer", referer).build();
        Call call = mOkHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private RequestBody setRequestBody(Map<String, String> BodyParams) {
        RequestBody body = null;
        okhttp3.FormBody.Builder formEncodingBuilder = new okhttp3.FormBody.Builder();
        if (BodyParams != null) {
            Iterator<String> iterator = BodyParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                formEncodingBuilder.add(key, BodyParams.get(key));
            }
        }
        body = formEncodingBuilder.build();
        return body;

    }

}
