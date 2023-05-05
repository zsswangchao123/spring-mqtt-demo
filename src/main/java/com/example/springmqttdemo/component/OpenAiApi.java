package com.example.springmqttdemo.component;

import com.example.springmqttdemo.model.ExecuteRet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OpenAiApi {

    @Value("${open.ai.url}")
    private String url;
    @Value("${open.ai.token}")
    private String token;

    private static final MultiThreadedHttpConnectionManager CONNECTION_MANAGER= new MultiThreadedHttpConnectionManager();

    static {
        // 默认单个host最大链接数
        CONNECTION_MANAGER.getParams().setDefaultMaxConnectionsPerHost(
                20);
        // 最大总连接数，默认20
        CONNECTION_MANAGER.getParams()
                .setMaxTotalConnections(20);
        // 连接超时时间
        CONNECTION_MANAGER.getParams()
                .setConnectionTimeout(60000);
        // 读取超时时间
        CONNECTION_MANAGER.getParams().setSoTimeout(60000);
    }

    public ExecuteRet get(String path, Map<String, String> headers) {
        GetMethod method = new GetMethod(url +path);
        if (headers== null) {
            headers = new HashMap<>();
        }
        headers.put("Authorization", "Bearer " + token);
        for (Map.Entry<String, String> h : headers.entrySet()) {
            method.setRequestHeader(h.getKey(), h.getValue());
        }
        return execute(method);
    }

    public ExecuteRet post(String path, String json, Map<String, String> headers) {
        try {
            PostMethod method = new PostMethod(url +path);
            //log.info("POST Url is {} ", url + path);
            // 输出传入参数
            log.info(String.format("POST JSON HttpMethod's Params = %s",json));
            StringRequestEntity entity = new StringRequestEntity(json, "application/json", "UTF-8");
            method.setRequestEntity(entity);
            if (headers== null) {
                headers = new HashMap<>();
            }
            headers.put("Authorization", "Bearer " + token);
            for (Map.Entry<String, String> h : headers.entrySet()) {
                method.setRequestHeader(h.getKey(), h.getValue());
            }
            return execute(method);
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getMessage(),ex);
        }
        return new ExecuteRet(false, "", null, -1);
    }

    public ExecuteRet execute(HttpMethod method) {
        HttpClient client = new HttpClient(CONNECTION_MANAGER);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("", "");
        client.getState().setProxyCredentials(AuthScope.ANY, credentials);
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setProxy("127.0.0.1", 7890);
        int statusCode = -1;
        String respStr = null;
        boolean isSuccess = false;
        try {
            client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF8");
            statusCode = client.executeMethod(hostConfiguration,method);
            method.getRequestHeaders();

            // log.info("执行结果statusCode = " + statusCode);
            InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder(100);
            String str;
            while ((str = reader.readLine()) != null) {
                log.debug("逐行读取String = " + str);
                stringBuffer.append(str.trim());
            }
            respStr = stringBuffer.toString();
            log.info(String.format("执行结果String = %s, Length = %d", respStr, respStr.length()));
            inputStreamReader.close();
            reader.close();
            // 返回200，接口调用成功
            isSuccess = (statusCode == HttpStatus.SC_OK);
        } catch (IOException ex) {
            log.error(ex.getMessage(),ex);
        } finally {
            method.releaseConnection();
        }
        return new ExecuteRet(isSuccess, respStr,method, statusCode);
    }

}
