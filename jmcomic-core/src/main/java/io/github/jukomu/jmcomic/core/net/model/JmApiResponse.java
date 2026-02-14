package io.github.jukomu.jmcomic.core.net.model;

import io.github.jukomu.jmcomic.api.exception.ResourceNotFoundException;
import io.github.jukomu.jmcomic.api.exception.ResponseException;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.crypto.JmCryptoTool;
import okhttp3.Response;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author JUKOMU
 * @Description: 禁漫API的JSON响应
 * @Project: jmcomic-api-java
 * @Date: 2025/10/31
 */
public class JmApiResponse extends JmResponse {

    // 请求API时使用的时间戳
    private final String timestamp;
    // 缓存解密后的数据
    private final AtomicReference<String> decodedDataCache = new AtomicReference<>();


    /**
     * 构造函数
     *
     * @param rawResponse 原始OkHttp响应对象
     */
    public JmApiResponse(Response rawResponse, String timestamp) {
        super(rawResponse);
        this.timestamp = timestamp;
    }

    public JmApiResponse(CommonResponse other, String timestamp) {
        super(other);
        this.timestamp = timestamp;
    }

    @Override
    public boolean isSuccess() {
        if (!super.isSuccess()) {
            return false;
        }
        // 检查API返回的code字段
        Object code = getJson().get("code");
        return code instanceof Number && ((Number) code).intValue() == 200;
    }

    @Override
    public void requireSuccess() throws ResponseException {
        super.requireSuccess();
        String decodedData = getDecodedData();
        // 判断本子是否存在
        if (decodedData.contains("\"name\":null") && decodedData.contains("\"images\":[]")) {
            throw new ResourceNotFoundException("请求的资源不存在", getOriginUrl());
        }
    }

    /**
     * 获取加密的API数据
     *
     * @return Base64编码的加密数据字符串
     */
    public String getEncodedData() {
        return String.valueOf(getJson().get("data"));
    }

    /**
     * 获取解密后的API数据
     *
     * @return 解密后的数据字符串
     */
    public String getDecodedData() {
        if (decodedDataCache.get() == null) {
            synchronized (decodedDataCache) {
                if (decodedDataCache.get() == null) {
                    decodedDataCache.set(JmCryptoTool.decryptApiResponse(getEncodedData(), timestamp, JmConstants.APP_DATA_SECRET));
                }
            }
        }
        return decodedDataCache.get();
    }
}
