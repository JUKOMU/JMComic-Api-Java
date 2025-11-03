package io.github.jukomu.jmcomic.sample.config;

import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.JmComic;

import java.time.Duration;

/**
 * @author JUKOMU
 * @Description: 配置示例
 * @Project: jmcomic-api-java
 * @Date: 2025/11/3
 */
public class ConfigUsage {
    public static void main(String[] args) {
        JmClient client1 = JmComic.newClient(getSimpleConfigUsage());
        JmClient client2 = JmComic.newClient(getAdvancedConfigUsage());
    }

    private static JmConfiguration getSimpleConfigUsage() {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();
        return config;
    }

    private static JmConfiguration getAdvancedConfigUsage() {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.HTML) // 切换为HTML客户端
                .proxy("127.0.0.1", 7890) // 设置HTTP代理
                .timeout(Duration.ofSeconds(60)) // 设置网络超时为60秒
                .retryTimes(10) // 设置最大重试次数
                .downloadThreadPoolSize(12) // 设置下载线程池大小
                .cacheSize(100*1024*1024) // 设置缓存池大小,单位: Byte
                .concurrentPhotoDownloads(2) // 设置同时下载的章节数
                .concurrentImageDownloads(15) // 设置同时下载的图片数
                .build();
        return config;
    }
}
