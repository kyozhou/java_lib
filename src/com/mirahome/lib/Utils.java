package com.mirahome.lib;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.HashMap;


/**
 * Created by zhoubin on 2017/7/6.
 */
public class Utils {

    public static String md5(Object input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(Utils.serialize(input));
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static JSON jsonSearch(JSON json, String sql) {
        JSON finalObj = null;

        return finalObj;
    }

    public static String getLocalIP() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
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

    public static String httpPost(String url, HashMap params, HashMap headers, MediaType mediaType) {
        final MediaType JSON = mediaType == null ? MediaType.parse("application/json; charset=utf-8") : mediaType;
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
