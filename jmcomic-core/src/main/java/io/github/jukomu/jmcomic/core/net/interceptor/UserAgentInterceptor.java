package io.github.jukomu.jmcomic.core.net.interceptor;

import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 一个OkHttp拦截器，负责根据客户端类型动态添加或覆盖 User-Agent 和其他必要的 Headers
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class UserAgentInterceptor implements Interceptor {

    private final JmConfiguration config;

    public UserAgentInterceptor(JmConfiguration config) {
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        // 确定默认的 Headers 和 User-Agent
        Map<String, String> defaultHeaders;
        if (config.getClientType() == ClientType.API) {
            defaultHeaders = JmConstants.DEFAULT_API_HEADERS;
        } else {
            defaultHeaders = JmConstants.DEFAULT_HTML_HEADERS;
        }

        // 1. 添加/覆盖默认 Headers
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            // 只有当请求中没有这个header时才添加，以尊重用户在单次请求中自定义的header
            if (originalRequest.header(entry.getKey()) == null) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        // 2. 添加/覆盖用户在 JmConfiguration 中指定的全局 Headers
        if (!config.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                // 用户配置的全局 Headers 优先级更高，会覆盖默认值
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}
