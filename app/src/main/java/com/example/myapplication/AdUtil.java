package com.example.myapplication;

import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdUtil {
    private static final String postUrl = "http://www.zhangyan.shop";
    public static final int AD_RECEIVED = 1;
    public static final int AD_REQUEST_ERROR = 2;

    private void requestAd(JSONObject jsonObject) {
        Handler handler = new Handler();
        try {
            URL url = new URL(postUrl);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            //设置超时时间
            conn.setConnectTimeout(1000);
            //转换为字节数组
            byte[] data = (jsonObject.toString()).getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");
            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write((jsonObject.toString()).getBytes());
            out.flush();
            out.close();

            if (conn.getResponseCode() == 200) {
                BufferedReader in = null;
                String result = "";
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
                Message msg = new Message();
                msg.what = AD_RECEIVED;
                msg.obj = result;
                handler.sendMessage(msg);
            } else {
                System.out.println("无广告返回");
                System.out.println(conn.getResponseMessage());
                Message msg = new Message();
                msg.what = AD_REQUEST_ERROR;
                msg.obj = conn.getResponseMessage();
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = AD_REQUEST_ERROR;
            msg.obj = "广告请求发生异常";
            handler.sendMessage(msg);
        }
    }
}
