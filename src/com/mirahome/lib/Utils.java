package com.mirahome.lib;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;


/**
 * Created by zhoubin on 2017/7/6.
 */
public class Utils {

    public static String getLocalIP() {

        try {
            String addr = InetAddress.getLocalHost().getHostAddress();
            return addr;
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String httpGet(String url, HashMap headers) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String httpPost(String url, HashMap params, HashMap headers) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        Request request = new Request.Builder()
                .headers(Headers.of(headers))
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
