package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 下载请求的 Builder，提供链式调用方式配置下载参数。
 * 通过 {@link JmClient#download(JmAlbum)} 或 {@link JmClient#download(JmPhoto)} 创建。
 *
 * <pre>{@code
 * DownloadResult result = client.download(album)
 *     .withPath(myPath)
 *     .withProgress(p -> updateUI(p))
 *     .execute();
 * }</pre>
 * <p>
 * 内部使用 {@link Function} 实现延迟执行，由客户端实现类（如 AbstractJmClient）
 * 在创建时注入实际执行逻辑，为未来支持取消、暂停等操作预留架构扩展点。
 */
public class DownloadRequest {

    private final Object entity;
    private final boolean isAlbum;
    private final Function<DownloadRequest, DownloadResult> executor;

    private Path path;
    private ExecutorService executorService;
    private Consumer<DownloadProgress> progressCallback;

    /**
     * 创建本子下载请求（内部使用，客户端实现类通过此构造器注入执行逻辑）。
     *
     * @param album    本子对象
     * @param executor 实际执行下载的函数，接收配置好的 DownloadRequest，返回 DownloadResult
     */
    public DownloadRequest(JmAlbum album, Function<DownloadRequest, DownloadResult> executor) {
        this.entity = Objects.requireNonNull(album);
        this.isAlbum = true;
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * 创建章节下载请求（内部使用，客户端实现类通过此构造器注入执行逻辑）。
     *
     * @param photo    章节对象
     * @param executor 实际执行下载的函数，接收配置好的 DownloadRequest，返回 DownloadResult
     */
    public DownloadRequest(JmPhoto photo, Function<DownloadRequest, DownloadResult> executor) {
        this.entity = Objects.requireNonNull(photo);
        this.isAlbum = false;
        this.executor = Objects.requireNonNull(executor);
    }

    // == Builder 方法 ==

    /**
     * 指定下载保存路径。若未调用此方法，使用默认路径生成策略。
     */
    public DownloadRequest withPath(Path path) {
        this.path = path;
        return this;
    }

    /**
     * 指定执行下载的线程池。若未调用此方法，使用客户端的默认线程池。
     */
    public DownloadRequest withExecutor(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * 注册下载进度回调。每完成一张图片或一个章节时回调一次。
     */
    public DownloadRequest withProgress(Consumer<DownloadProgress> progressCallback) {
        this.progressCallback = progressCallback;
        return this;
    }

    /**
     * 执行下载。
     *
     * @return 下载结果报告
     * @throws UnsupportedOperationException 如果创建此请求的客户端实现未提供执行逻辑
     */
    public DownloadResult execute() {
        return executor.apply(this);
    }

    // == 内部 Getter（供客户端实现类读取配置）==

    public boolean isAlbum() {
        return isAlbum;
    }

    public JmAlbum getAlbum() {
        return (JmAlbum) entity;
    }

    public JmPhoto getPhoto() {
        return (JmPhoto) entity;
    }

    public Path getPath() {
        return path;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Consumer<DownloadProgress> getProgressCallback() {
        return progressCallback;
    }
}
