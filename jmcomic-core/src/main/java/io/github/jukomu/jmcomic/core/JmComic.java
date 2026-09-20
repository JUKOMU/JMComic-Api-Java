package io.github.jukomu.jmcomic.core;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.client.impl.JmApiClient;
import io.github.jukomu.jmcomic.core.client.impl.JmHtmlClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.net.OkHttpBuilder;

import java.util.concurrent.CompletableFuture;

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
     * @deprecated 使用 {@link #newApiClientAsync(JmConfiguration)} 等待客户端完成初始化并观察失败
     */
    @Deprecated
    public static JmApiClient newApiClient(JmConfiguration config) {
        JmApiClient client = createApiClient(config);
        client.initializeAsync();
        return client;
    }

    /**
     * 根据配置异步创建一个新的 JmApiClient 实例。
     * 返回的 Future 仅在客户端完整初始化后成功。
     *
     * @param config 客户端的配置对象
     * @return 初始化完成后的 JmApiClient Future
     */
    public static CompletableFuture<JmApiClient> newApiClientAsync(JmConfiguration config) {
        return initializeClient(createApiClient(config));
    }

    /**
     * 根据配置创建一个新的 JmHtmlClient 实例。
     *
     * @param config 客户端的配置对象
     * @return JmHtmlClient
     * @deprecated 使用 {@link #newHtmlClientAsync(JmConfiguration)} 等待客户端完成初始化并观察失败
     */
    @Deprecated
    public static JmHtmlClient newHtmlClient(JmConfiguration config) {
        JmHtmlClient client = createHtmlClient(config);
        client.initializeAsync();
        return client;
    }

    /**
     * 根据配置异步创建一个新的 JmHtmlClient 实例。
     * 返回的 Future 仅在客户端完整初始化后成功。
     *
     * @param config 客户端的配置对象
     * @return 初始化完成后的 JmHtmlClient Future
     */
    public static CompletableFuture<JmHtmlClient> newHtmlClientAsync(JmConfiguration config) {
        return initializeClient(createHtmlClient(config));
    }

    private static JmApiClient createApiClient(JmConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        if (config.getClientType() == ClientType.HTML) {
            throw new IllegalArgumentException("Cannot create ApiClient with HTML client type.");
        }
        OkHttpBuilder.HttpClientContext context = OkHttpBuilder.build(config);
        return new JmApiClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
    }

    private static JmHtmlClient createHtmlClient(JmConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        if (config.getClientType() == ClientType.API) {
            throw new IllegalArgumentException("Cannot create HtmlClient with API client type.");
        }
        OkHttpBuilder.HttpClientContext context = OkHttpBuilder.build(config);
        return new JmHtmlClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
    }

    private static <T extends AbstractJmClient> CompletableFuture<T> initializeClient(T client) {
        CompletableFuture<T> result = new CompletableFuture<>();
        client.initializeAsync().whenComplete((ignored, error) -> {
            if (error == null) {
                result.complete(client);
            } else {
                result.completeExceptionally(error);
            }
        });
        result.whenComplete((ignored, error) -> {
            if (result.isCancelled()) {
                client.close();
            }
        });
        return result;
    }
}
