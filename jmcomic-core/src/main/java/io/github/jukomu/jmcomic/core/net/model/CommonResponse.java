package io.github.jukomu.jmcomic.core.net.model;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author JUKOMU
 * @Description: 对OkHttp Response的通用封装
 * @Project: jmcomic-api-java
 * @Date: 2025/10/30
 */
public class CommonResponse {

    // 原始OkHttp Response对象
    protected final Response rawResponse;

    // -- 缓存字段 --

    // 使用volatile确保多线程可见性
    protected volatile byte[] cachedContent;
    // 用于同步的锁对象
    private final Object contentLock = new Object();

    public CommonResponse(Response rawResponse) {
        if (rawResponse == null) {
            throw new IllegalArgumentException("Raw OkHttp Response cannot be null.");
        }
        this.rawResponse = rawResponse;
    }

    /**
     * 获取HTTP状态码
     *
     * @return HTTP状态码
     */
    public int getHttpCode() {
        return rawResponse.code();
    }

    /**
     * 判断请求是否成功（通常为200）
     *
     * @return 如果成功返回 true
     */
    public boolean isSuccess() {
        // OkHttp's isSuccessful() checks 200-299 range
        return rawResponse.isSuccessful();
    }

    /**
     * 判断请求是否不成功
     *
     * @return 如果不成功返回 true
     */
    public boolean isNotSuccess() {
        return !isSuccess();
    }

    /**
     * 如果请求不成功则抛出AssertionError
     *
     * @throws AssertionError 如果请求不成功
     */
    public void requireSuccess() throws AssertionError {
        if (isNotSuccess()) {
            throw new AssertionError("Response is not successful. HTTP Code: " + getHttpCode() + ", URL: " + getUrl());
        }
    }

    /**
     * 获取响应文本内容
     *
     * @return 响应文本
     */
    public String getText() {
        // 从缓存的字节数组中获取文本
        byte[] content = getContent();
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * 获取响应的URL
     *
     * @return URL字符串
     */
    public String getUrl() {
        return rawResponse.request().url().toString();
    }

    /**
     * 获取响应头
     *
     * @return 响应头Map
     */
    public Map<String, List<String>> getHeaders() {
        return rawResponse.headers().toMultimap();
    }

    /**
     * 获取响应的原始字节内容
     *
     * @return 字节数组
     */
    public byte[] getContent() {
        // 实现一次性读取和缓存
        if (cachedContent == null) {
            synchronized (contentLock) {
                // 双重检查锁定，防止多个线程同时读取
                if (cachedContent == null) {
                    ResponseBody body = rawResponse.body();
                    if (body == null) {
                        cachedContent = new byte[0];
                        return cachedContent;
                    }
                    try {
                        byte[] rawBytes = body.bytes(); // 读取并关闭响应体

                        // 检查是否需要GZIP解压
                        if ("gzip".equalsIgnoreCase(rawResponse.header("Content-Encoding"))) {
                            // 如果响应是GZIP编码，则解压
                            cachedContent = decompressGzip(rawBytes);
                        } else {
                            // 否则直接使用原始字节
                            cachedContent = rawBytes;
                        }
                    } catch (IllegalStateException e) {
                        // 如果响应已被关闭，返回空内容
                        cachedContent = new byte[0];
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedContent;
    }

    /**
     * 解压GZIP数据
     */
    private byte[] decompressGzip(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             GZIPInputStream gis = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            gis.transferTo(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 获取原始URL
     *
     * @return 原始请求URL
     */
    public String getOriginUrl() {
        Response current = rawResponse;
        String originUrl = current.request().url().toString();
        while (current != null) {
            originUrl = current.request().url().toString();
            current = current.priorResponse();
        }
        return originUrl;
    }

    /**
     * 获取重定向URL（如果存在Location头）
     *
     * @return 重定向URL字符串
     */
    public String getRedirectUrl() {
        String location = rawResponse.header("Location");
        return location != null ? location : getUrl();
    }

    /**
     * 将响应解析为Map
     *
     * @return JSON Map
     * @throws JSONException 如果解析失败
     */
    public Map<String, Object> getMap() {
        String text = getText();
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }
        return JSONObject.parseObject(text);
    }

    /**
     * 将响应解析为JSON
     *
     * @return JSON
     * @throws JSONException 如果解析失败
     */
    public Map<String, Object> getJson() {
        return getMap();
    }

    /**
     * 获取原始响应对象
     *
     * @param <T> 原始响应类型
     * @return 原始响应对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getRawResponse() {
        return (T) rawResponse;
    }

    /**
     * 判断是否重定向
     *
     * @return 是否重定向
     */
    public boolean isRedirect() {
        return getRedirectCount() > 0;
    }

    /**
     * 获取HTTP响应的重定向次数
     *
     * @return 重定向次数
     */
    public int getRedirectCount() {
        int count = 0;
        Response current = rawResponse;
        while (current.priorResponse() != null) {
            count++;
            current = current.priorResponse();
        }
        return count;
    }

    public String toString() {
        return "CommonResponse{" +
                "httpCode=" + getHttpCode() +
                ", url='" + getUrl() + '\'' +
                ", success=" + isSuccess() +
                '}';
    }
}