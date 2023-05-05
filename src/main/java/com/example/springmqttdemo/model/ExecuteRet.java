package com.example.springmqttdemo.model;

import org.apache.commons.httpclient.HttpMethod;

/**
 * 调用返回
 */
public class ExecuteRet {

    /**
     * 操作是否成功
     */
    private final boolean success;

    /**
     * 返回的内容
     */
    private final String respStr;

    /**
     * 请求的地址
     */
    private final HttpMethod method;

    /**
     * statusCode
     */
    private final int statusCode;

    public ExecuteRet(boolean success, String respStr, HttpMethod method, int statusCode) {
        this.success =success;
        this.respStr =respStr;
        this.method =method;
        this.statusCode =statusCode;
    }

    @Override
    public String toString() {
        return String.format("[success:%s,respStr:%s,statusCode:%s]", success, respStr, statusCode);
    }

    /**
     *@return the isSuccess
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     *@return the !isSuccess
     */
    public boolean isNotSuccess() {
        return !success;
    }

    /**
     *@return the respStr
     */
    public String getRespStr() {
        return respStr;
    }

    /**
     *@return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     *@return the method
     */
    public HttpMethod getMethod() {
        return method;
    }
}
