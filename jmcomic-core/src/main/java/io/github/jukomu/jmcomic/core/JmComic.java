package io.github.jukomu.jmcomic.core;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.client.impl.JmApiClient;
import io.github.jukomu.jmcomic.core.client.impl.JmHtmlClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.net.OkHttpBuilder;

/**
 * @author JUKOMU
 * @Description: 入口工厂类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmComic {

    /**
     * 私有构造函数，防止此类被实例化。
     */
    private JmComic() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 根据配置创建一个新的 JmApiClient 实例
     *
     * @param config 客户端的配置对象
     * @return JmApiClient
     */
    public static JmApiClient newApiClient(JmConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        if (config.getClientType() == ClientType.HTML) {
            throw new IllegalArgumentException("Cannot create ApiClient with HTML client type.");
        }
        OkHttpBuilder.HttpClientContext context = OkHttpBuilder.build(config);
        return new JmApiClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
    }

    /**
     * 根据配置创建一个新的 JmHtmlClient 实例。
     *
     * @param config 客户端的配置对象
     * @return JmHtmlClient
     */
    public static JmHtmlClient newHtmlClient(JmConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        if (config.getClientType() == ClientType.API) {
            throw new IllegalArgumentException("Cannot create HtmlClient with API client type.");
        }
        OkHttpBuilder.HttpClientContext context = OkHttpBuilder.build(config);
        return new JmHtmlClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
    }
}
