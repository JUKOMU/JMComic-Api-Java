package io.github.jukomu.jmcomic.core.config;

import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.cache.CacheKey;
import io.github.jukomu.jmcomic.core.cache.CachePool;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author JUKOMU
 * @Description: JmClient 的不可变配置对象
 * 使用 {@link Builder} 模式进行构建
 * 此对象封装了所有用于创建和定制 JmClient 实例所需的信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmConfiguration {
    // 客户端类型
    private final ClientType clientType;
    // 存储API客户端的域名
    private final List<String> apiDomains;
    // 存储HTML客户端的域名
    private final List<String> htmlDomains;
    // 代理设置
    private final Proxy proxy;
    // 请求头
    private final Map<String, String> headers;
    // 超时时间
    private final Duration timeout;
    // 重试次数
    private final int retryTimes;
    // 请求的线程池
    private final ExecutorService executor;
    // 线程池大小
    private final int downloadThreadPoolSize;
    // 缓存大小, 单位: Byte
    private final CachePool<CacheKey, Object> cachePool;
    /**
     * @deprecated 线程池大小已统一控制并发，此配置项不再生效
     */
    @Deprecated
    private final int concurrentPhotoDownloads;
    /**
     * @deprecated 线程池大小已统一控制并发，此配置项不再生效
     */
    @Deprecated
    private final int concurrentImageDownloads;
    // 后台域名复探间隔（毫秒），默认10分钟
    private final long domainProbeIntervalMs;
    // 初始化探活单域名超时（毫秒），默认3秒
    private final long domainProbeTimeoutMs;
    // 图片下载超时
    private final Duration imageTimeout;
    // 关闭时等待进行中任务完成的超时（毫秒）
    private final long closeTimeoutMs;

    private JmConfiguration(Builder builder) {
        this.clientType = builder.clientType;
        this.apiDomains = Collections.unmodifiableList(builder.apiDomains);
        this.htmlDomains = Collections.unmodifiableList(builder.htmlDomains);
        this.proxy = builder.proxy;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.timeout = builder.timeout;
        this.retryTimes = builder.retryTimes;
        this.executor = builder.executor;
        this.downloadThreadPoolSize = builder.downloadThreadPoolSize;
        this.cachePool = new CachePool<>(builder.cacheSize);
        this.concurrentPhotoDownloads = builder.concurrentPhotoDownloads;
        this.concurrentImageDownloads = builder.concurrentImageDownloads;
        this.domainProbeIntervalMs = builder.domainProbeIntervalMs;
        this.domainProbeTimeoutMs = builder.domainProbeTimeoutMs;
        this.imageTimeout = builder.imageTimeout;
        this.closeTimeoutMs = builder.closeTimeoutMs;
    }

    // Getters for all fields
    public ClientType getClientType() {
        return clientType;
    }

    public List<String> getApiDomains() {
        return apiDomains;
    }

    public List<String> getHtmlDomains() {
        return htmlDomains;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public int getDownloadThreadPoolSize() {
        return downloadThreadPoolSize;
    }

    public CachePool<CacheKey, Object> getCachePool() {
        return cachePool;
    }

    /**
     * @deprecated 线程池大小已统一控制并发，此配置项不再生效
     */
    @Deprecated
    public int getConcurrentPhotoDownloads() {
        return concurrentPhotoDownloads;
    }

    /**
     * @deprecated 线程池大小已统一控制并发，此配置项不再生效
     */
    @Deprecated
    public int getConcurrentImageDownloads() {
        return concurrentImageDownloads;
    }

    public long getDomainProbeIntervalMs() {
        return domainProbeIntervalMs;
    }

    public long getDomainProbeTimeoutMs() {
        return domainProbeTimeoutMs;
    }

    public Duration getImageTimeout() {
        return imageTimeout;
    }

    public long getCloseTimeoutMs() {
        return closeTimeoutMs;
    }

    /**
     * 用于创建 JmConfiguration 实例的 Builder
     */
    public static class Builder {
        private ClientType clientType = ClientType.API;
        private List<String> apiDomains = new ArrayList<>();
        private List<String> htmlDomains = new ArrayList<>();
        private Proxy proxy;
        private Map<String, String> headers = new HashMap<>();
        private Duration timeout = Duration.ofSeconds(30);
        private int retryTimes = 5;
        private ExecutorService executor = null;
        private int downloadThreadPoolSize = -1; // -1 表示使用默认值 (CPU核心数)
        private int cacheSize = 100 * 1024 * 1024;
        private int concurrentPhotoDownloads = 3;
        private int concurrentImageDownloads = 20;
        private long domainProbeIntervalMs = 10 * 60 * 1000; // 10分钟
        private long domainProbeTimeoutMs = 3000;            // 3秒
        private Duration imageTimeout = Duration.ofSeconds(60);
        private long closeTimeoutMs = 60_000;                // 60秒

        public Builder clientType(ClientType type) {
            this.clientType = Objects.requireNonNull(type);
            return this;
        }

        public Builder apiDomains(List<String> domains) {
            this.apiDomains = new ArrayList<>(Objects.requireNonNull(domains));
            return this;
        }

        public Builder htmlDomains(List<String> domains) {
            this.htmlDomains = new ArrayList<>(Objects.requireNonNull(domains));
            return this;
        }

        public Builder proxy(String host, int port) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder retryTimes(int retryTimes) {
            if (retryTimes < 0) throw new IllegalArgumentException("Retry times must be non-negative.");
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder downloadThreadPoolSize(int size) {
            if (size < -1 || size == 0) throw new IllegalArgumentException("Thread pool size must be positive or -1.");
            this.downloadThreadPoolSize = size;
            return this;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder cacheSize(int size) {
            if (size < 0) throw new IllegalArgumentException("Cache size must be non-negative.");
            this.cacheSize = size;
            return this;
        }

        /**
         * @deprecated 线程池大小已统一控制并发，此配置项不再生效
         */
        @Deprecated
        public Builder concurrentPhotoDownloads(int size) {
            if (size < 0) throw new IllegalArgumentException("Concurrent photo uploads must be non-negative.");
            this.concurrentPhotoDownloads = size;
            return this;
        }

        /**
         * @deprecated 线程池大小已统一控制并发，此配置项不再生效
         */
        @Deprecated
        public Builder concurrentImageDownloads(int size) {
            if (size < 0) throw new IllegalArgumentException("Concurrent image uploads must be non-negative.");
            this.concurrentImageDownloads = size;
            return this;
        }

        public Builder domainProbeIntervalMs(long intervalMs) {
            if (intervalMs < 0) throw new IllegalArgumentException("Domain probe interval must be non-negative.");
            this.domainProbeIntervalMs = intervalMs;
            return this;
        }

        public Builder domainProbeTimeoutMs(long timeoutMs) {
            if (timeoutMs < 0) throw new IllegalArgumentException("Domain probe timeout must be non-negative.");
            this.domainProbeTimeoutMs = timeoutMs;
            return this;
        }

        public Builder imageTimeout(Duration timeout) {

            this.imageTimeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder closeTimeoutMs(long timeoutMs) {
            if (timeoutMs < 0) throw new IllegalArgumentException("Close timeout must be non-negative.");
            this.closeTimeoutMs = timeoutMs;
            return this;
        }

        public Builder loadFromProperties(InputStream inputStream) throws IOException {
            Properties props = new Properties();
            props.load(inputStream);

            if (props.containsKey("client.type")) {
                this.clientType(ClientType.valueOf(props.getProperty("client.type").toUpperCase()));
            }
            if (props.containsKey("proxy.host") && props.containsKey("proxy.port")) {
                this.proxy(props.getProperty("proxy.host"), Integer.parseInt(props.getProperty("proxy.port")));
            }
            if (props.containsKey("retry.times")) {
                this.retryTimes(Integer.parseInt(props.getProperty("retry.times")));
            }
            // 可以根据需要添加更多从 properties 加载的配置项

            return this;
        }

        public JmConfiguration build() {
            if (this.executor != null) {
                this.downloadThreadPoolSize = -1;
            }
            return new JmConfiguration(this);
        }
    }
}
