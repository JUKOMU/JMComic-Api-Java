package io.github.jukomu.jmcomic.sample.config;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

import java.time.Duration;

/**
 * 配置示例，演示 {@link JmConfiguration.Builder} 的常用选项。
 *
 * <p>运行此示例不会发起网络请求，仅演示配置构建。
 */
public class ConfigUsage {
    public static void main(String[] args) {
        System.out.println("=== 简单配置 ===");
        JmConfiguration simple = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();
        System.out.println("客户端类型: " + simple.getClientType());
        System.out.println("线程池大小: " + simple.getDownloadThreadPoolSize());

        System.out.println("\n=== 完整配置 ===");
        JmConfiguration advanced = new JmConfiguration.Builder()
                .clientType(ClientType.HTML)
                .proxy("127.0.0.1", 7890)
                .timeout(Duration.ofSeconds(60))
                .retryTimes(10)
                .downloadThreadPoolSize(12)
                .imageTimeout(Duration.ofSeconds(120))
                .closeTimeoutMs(30_000)
                .domainProbeIntervalMs(600_000)
                .domainProbeTimeoutMs(3000)
                .cacheSize(100 * 1024 * 1024)
                .build();
        System.out.println("客户端类型: " + advanced.getClientType());
        System.out.println("线程池大小: " + advanced.getDownloadThreadPoolSize());
        System.out.println("超时: " + advanced.getTimeout().getSeconds() + "s");
        System.out.println("重试次数: " + advanced.getRetryTimes());

        // 用完整配置创建客户端，验证配置是否生效
        try (AbstractJmClient client = JmComic.newHtmlClient(advanced)) {
            System.out.println("\n客户端已创建: " + client.getClientType());
        }
    }
}
