package io.github.jukomu.jmcomic.api.net;

/**
 * @author JUKOMU
 * @Description: Cloudflare 验证接口
 * @Project: jmcomic-api-java
 * @Date: 2026-02-14
 */
public interface CloudflareChallengeSolver {
    /**
     * 当检测到 Cloudflare 拦截时被调用
     *
     * @param url 触发拦截的 URL
     * @return 解决方案（包含通过验证后的 Cookie 和 User-Agent）
     */
    Solution solve(String url);

    class Solution {
        private final String cookies;
        private final String userAgent;

        public Solution(String cookies, String userAgent) {
            this.cookies = cookies;
            this.userAgent = userAgent;
        }

        public String getCookies() {
            return cookies;
        }

        public String getUserAgent() {
            return userAgent;
        }

        @Override
        public String toString() {
            return "Solution{ua='" + userAgent + "', cookies='" + cookies + "'}";
        }
    }
}
