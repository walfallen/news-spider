package com.yc.spider.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by youmingwei on 17/4/13.
 */
public class MonitorUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static void alertToDingDing(String msg) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "https://oapi.dingtalk.com/robot/send?access_token=" +
                "444a34dddcc910afd36449e44b920a92cb6d6aa81653e2e0f2ca275f0192c299";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        ObjectNode param = objectMapper.createObjectNode();
        param.put("msgtype", "text");
        param.set("text", objectMapper.createObjectNode().put("content", msg));
        param.set("at", objectMapper.createObjectNode().put("isAtAll", false));
        StringEntity stringEntity = new StringEntity(param.toString(), "utf-8");
        httpPost.setEntity(stringEntity);
        try {
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
