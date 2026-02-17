package io.github.jukomu.jmcomic.android.support;

import android.content.Context;
import android.content.Intent;
import io.github.jukomu.jmcomic.api.net.CloudflareChallengeSolver;

import java.util.concurrent.CompletableFuture;

/**
 * @author JUKOMU
 * @Description: CloudflareChallengeSolver 的一个实现，基于 Android WebView
 * @Project: jmcomic-api-java
 * @Date: 2026-02-14
 */
public class AndroidWebViewSolver implements CloudflareChallengeSolver {
    private final Context appContext;
    // 使用 CompletableFuture 在后台线程和 UI 线程之间同步结果
    public static volatile CompletableFuture<Solution> pendingFuture;

    public AndroidWebViewSolver(Context context) {
        // 确保使用 Application Context 避免内存泄露
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Solution solve(String url) {
        // 这个方法必须在后台线程中调用，否则 .get() 会阻塞主线程
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            throw new IllegalStateException("ChallengeSolver.solve() cannot be called on the main thread.");
        }

        pendingFuture = new CompletableFuture<>();

        try {
            Intent intent = new Intent(appContext, CloudflareWebViewActivity.class);
            intent.putExtra("url", url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);

            // 阻塞等待 Activity 的结果，超时 3 分钟
            return pendingFuture.get(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            pendingFuture = null;
        }
    }
}
