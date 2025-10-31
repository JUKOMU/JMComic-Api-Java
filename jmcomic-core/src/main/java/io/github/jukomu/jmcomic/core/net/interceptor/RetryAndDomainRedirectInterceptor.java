package io.github.jukomu.jmcomic.core.net.interceptor;

import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author JUKOMU
 * @Description: 一个OkHttp拦截器，负责实现核心的重试和域名动态切换逻辑
 * 这个拦截器是有状态的，因为它持有一个 JmDomainManager 实例
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class RetryAndDomainRedirectInterceptor implements Interceptor {

    private final JmDomainManager domainManager;
    private final int maxRetriesPerRequest;

    public RetryAndDomainRedirectInterceptor(int maxRetries, JmDomainManager domainManager) {
        this.maxRetriesPerRequest = maxRetries;
        this.domainManager = domainManager;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        IOException lastException = null;

        for (int tryCount = 0; tryCount <= maxRetriesPerRequest; tryCount++) {
            // 获取当前最佳域名
            String bestDomain = domainManager.getBestDomain();
            if (bestDomain == null) {
                throw new IOException("No available domains to try. All domains have been marked as failed.", lastException);
            }

            Request requestToProceed;
            final boolean isPlaceholder = isPlaceholderRequest(originalRequest);

            if (isPlaceholder) {
                // 如果是占位符请求，总是用最佳域名替换
                Request requestToProceed1 = replaceHost(originalRequest, bestDomain);
                requestToProceed = addAddHeaders(requestToProceed1, bestDomain);
            } else {
                requestToProceed = addAddHeaders(originalRequest, "18comic.vip");
            }

            String currentHost = requestToProceed.url().host();

            try {
                Response response = chain.proceed(requestToProceed);

                if (response.isSuccessful()) {
                    domainManager.reportSuccess(currentHost);
                    return response;
                }

                // 服务端错误 (HTTP 5xx)，报告失败，关闭响应，然后继续循环重试
                if (response.code() >= 500) {
                    domainManager.reportFailure(currentHost);
                    response.close();
                    lastException = new IOException("Server error: " + response.code() + " for host " + currentHost);
                    continue;
                }

                // 其他非成功响应 (4xx, 3xx)，不重试，直接返回
                return response;

            } catch (IOException e) {
                domainManager.reportFailure(currentHost);
                lastException = e;
            }
        }

        // 如果循环结束仍未成功，抛出最后一次记录的异常
        throw new IOException("Request failed after " + maxRetriesPerRequest + " retries for URL: " + originalRequest.url(), lastException);
    }

    /**
     * 检查请求是否使用了占位符
     */
    private boolean isPlaceholderRequest(Request request) {
        return JmConstants.PLACEHOLDER_HOST.equals(request.url().host());
    }

    /**
     * 将请求的 host 替换为指定的域名。
     */
    private Request replaceHost(Request request, String newHost) {
        HttpUrl newUrl = request.url().newBuilder()
                .host(newHost)
                .build();
        return request.newBuilder()
                .url(newUrl)
                .header("Host", newHost) // 确保Host头被正确设置
                .build();
    }

    private Request addAddHeaders(Request request, String newHost) {
        HttpUrl newUrl = request.url().newBuilder()
                .build();
        String origin = "https://" + newHost;
        return request.newBuilder()
                .url(newUrl)
                .header("Authority", newHost)
                .header("Origin", origin)
                .header("Referer", origin)
                .build();
    }
}