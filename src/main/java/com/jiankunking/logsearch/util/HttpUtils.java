package com.jiankunking.logsearch.util;


import com.jiankunking.logsearch.config.GlobalConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@Component
public class HttpUtils {

    private OkHttpClient okHttpClient;
    private MediaType jsonMediaType = MediaType.parse(GlobalConfig.CONTENT_TYPE);

    private HttpUtils() {
        okHttpClient = new OkHttpClient();
        okHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
    }

    public okhttp3.Response post(String url, String json, OkHttpClient client) throws IOException {
        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonMediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        if (client != null) {
            return client.newCall(request).execute();
        }
        return okHttpClient.newCall(request).execute();
    }

    public okhttp3.Response get(String url, OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        if (client != null) {
            return client.newCall(request).execute();
        }
        return okHttpClient.newCall(request).execute();
    }
}
