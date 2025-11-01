package io.github.jukomu.jmcomic.api.config;

import io.github.jukomu.jmcomic.api.enums.ClientType;

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

    private final ClientType clientType;
    private final List<String> apiDomains;
    private final List<String> htmlDomains;
    private final Proxy proxy;
    private final Map<String, String> headers;
    private final Duration timeout;
    private final int retryTimes;
    private final ExecutorService downloadExecutor;
    private final int downloadThreadPoolSize;
    // 单位: Byte
    private final int cacheSize;

    private JmConfiguration(Builder builder) {
        this.clientType = builder.clientType;
        this.apiDomains = Collections.unmodifiableList(builder.apiDomains);
        this.htmlDomains = Collections.unmodifiableList(builder.htmlDomains);
        this.proxy = builder.proxy;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.timeout = builder.timeout;
        this.retryTimes = builder.retryTimes;
        this.downloadExecutor = builder.downloadExecutor;
        this.downloadThreadPoolSize = builder.downloadThreadPoolSize;
        this.cacheSize = builder.cacheSize;
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

    public ExecutorService getDownloadExecutor() {
        return downloadExecutor;
    }

    public int getDownloadThreadPoolSize() {
        return downloadThreadPoolSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * 用于创建 JmConfiguration 实例的 Builder
     */
    public static class Builder {
        private ClientType clientType = ClientType.API;
        private List<String> apiDomains = new java.util.ArrayList<>();
        private List<String> htmlDomains = new java.util.ArrayList<>();
        private Proxy proxy;
        private Map<String, String> headers = new HashMap<>();
        private Duration timeout = Duration.ofSeconds(30);
        private int retryTimes = 5;
        private ExecutorService downloadExecutor = null;
        private int downloadThreadPoolSize = -1; // -1 表示使用默认值 (CPU核心数)
        private int cacheSize = 100 * 1024 * 1024;

        public Builder clientType(ClientType type) {
            this.clientType = Objects.requireNonNull(type);
            return this;
        }

        public Builder apiDomains(List<String> domains) {
            this.apiDomains = new java.util.ArrayList<>(Objects.requireNonNull(domains));
            return this;
        }

        public Builder htmlDomains(List<String> domains) {
            this.htmlDomains = new java.util.ArrayList<>(Objects.requireNonNull(domains));
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
            if (size <= 0) throw new IllegalArgumentException("Thread pool size must be positive.");
            this.downloadThreadPoolSize = size;
            return this;
        }

        public Builder downloadExecutor(ExecutorService executor) {
            this.downloadExecutor = executor;
            return this;
        }

        public Builder cacheSize(int size) {
            if (size < 0) throw new IllegalArgumentException("Cache size must be non-negative.");
            this.cacheSize = size;
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
            if (this.downloadExecutor != null) {
                this.downloadThreadPoolSize = -1;
            }
            return new JmConfiguration(this);
        }
    }
}
