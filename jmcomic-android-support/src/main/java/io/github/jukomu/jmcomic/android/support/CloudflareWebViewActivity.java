package io.github.jukomu.jmcomic.android.support;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import io.github.jukomu.jmcomic.api.net.CloudflareChallengeSolver.Solution;

/**
 * @author JUKOMU
 * @Description: 用于显示 Cloudflare 验证界面的 Activity
 * @Project: jmcomic-api-java
 * @date 2026-02-14
 */
public class CloudflareWebViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);

        String url = getIntent().getStringExtra("url");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                checkCookie(url, view.getSettings().getUserAgentString());
            }

            private void checkCookie(String currentUrl, String userAgent) {
                String cookie = CookieManager.getInstance().getCookie(currentUrl);
                if (cookie != null && cookie.contains("cf_clearance")) {
                    if (AndroidWebViewSolver.pendingFuture != null && !AndroidWebViewSolver.pendingFuture.isDone()) {
                        AndroidWebViewSolver.pendingFuture.complete(new Solution(cookie, userAgent));
                        finish(); // 任务完成，关闭 Activity
                    }
                }
            }
        });

        webView.loadUrl(url);
    }
}
