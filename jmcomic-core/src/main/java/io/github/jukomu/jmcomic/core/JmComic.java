package io.github.jukomu.jmcomic.core;

import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.client.impl.JmApiClient;
import io.github.jukomu.jmcomic.core.client.impl.JmHtmlClient;
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
     * 根据提供的配置创建一个新的 JmClient 实例。
     * 这是获取客户端实例的唯一推荐方式。
     *
     * @param config 客户端的配置对象。不能为空。
     * @return 一个根据配置初始化的 JmClient 实例。
     * @throws IllegalArgumentException 如果配置中的 clientType 不被支持。
     */
    public static JmClient newClient(JmConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }

        // 构建包含所有网络组件的上下文
        OkHttpBuilder.HttpClientContext context = OkHttpBuilder.build(config);

        JmClient client;
        // 根据 ClientType，将上下文中的组件注入到客户端实现中
        ClientType clientType = config.getClientType();
        switch (clientType) {
            case API:
                client = new JmApiClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
                break;
            case HTML:
                client = new JmHtmlClient(config, context.getClient(), context.getCookieManager(), context.getDomainManager());
                break;
            default:
                throw new IllegalArgumentException("Unsupported client type: " + clientType);
        }

        return client;
    }
}
