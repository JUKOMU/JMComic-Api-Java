package io.github.jukomu.jmcomic.core.net.interceptor;

import io.github.jukomu.jmcomic.api.net.CloudflareChallengeSolver;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author JUKOMU
 * @Description: 一个OkHttp拦截器，负责检测 403 错误并调用 CloudflareChallengeSolver
 * @Project: jmcomic-api-java
 * @Date: 2026-02-14
 */
public class CloudflareInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(CloudflareInterceptor.class);

    private final CloudflareChallengeSolver solver;
    private volatile String cachedCookie;
    private volatile String cachedUserAgent;

    public CloudflareInterceptor(CloudflareChallengeSolver solver) {
        this.solver = solver;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // 1. 注入已缓存的凭证
        if (cachedCookie != null && cachedUserAgent != null) {
            requestBuilder.header("Cookie", cachedCookie);
            requestBuilder.header("User-Agent", cachedUserAgent);
        }

        Response response = chain.proceed(requestBuilder.build());

        // 2. 检测拦截
        if (isCloudflareIntercepted(response)) {
            synchronized (this) {
                // 双重检查
                if (cachedCookie != null && cachedUserAgent != null) {
                    response.close();
                    // 使用缓存重试
                    return chain.proceed(rebuildRequest(originalRequest, cachedCookie, cachedUserAgent));
                }

                logger.warn("检测到拦截 (403)，正在请求 Cloudflare 验证...");
                String url = originalRequest.url().toString();
                // 释放连接
                response.close();

                try {
                    // 3. 调用解决器
                    CloudflareChallengeSolver.Solution solution = solver.solve(url);

                    if (solution != null) {
                        logger.info("Cloudflare 验证成功，更新凭证");
                        this.cachedCookie = solution.getCookies();
                        this.cachedUserAgent = solution.getUserAgent();

                        // 4. 重试
                        return chain.proceed(rebuildRequest(originalRequest, cachedCookie, cachedUserAgent));
                    } else {
                        throw new IOException("Cloudflare 验证失败或用户取消");
                    }
                } catch (Exception e) {
                    throw new IOException("Cloudflare 验证过程中发生错误", e);
                }
            }
        }
        return response;
    }

    private boolean isCloudflareIntercepted(Response response) {
        if (response.code() == 403 || response.code() == 503) {
            String server = response.header("Server");
            return "cloudflare".equalsIgnoreCase(server);
        }
        return false;
    }

    private Request rebuildRequest(Request original, String cookie, String ua) {
        return original.newBuilder()
                .header("Cookie", cookie)
                .header("User-Agent", ua)
                .build();
    }
}
