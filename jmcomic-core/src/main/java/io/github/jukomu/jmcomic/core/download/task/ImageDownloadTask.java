package io.github.jukomu.jmcomic.core.download.task;

import io.github.jukomu.jmcomic.api.download.DownloadProgress;
import io.github.jukomu.jmcomic.api.download.enums.TaskState;
import io.github.jukomu.jmcomic.api.download.task.BaseDownloadTask;
import io.github.jukomu.jmcomic.api.exception.NetworkException;
import io.github.jukomu.jmcomic.api.exception.ResponseException;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.crypto.JmImageTool;
import io.github.jukomu.jmcomic.core.download.DownloadManager;
import io.github.jukomu.jmcomic.core.util.FileUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

/**
 * @author JUKOMU
 * @Description: 图片下载任务
 * @Project: JMComic-Api-Java
 * @Date: 2026/5/4
 */
public class ImageDownloadTask extends BaseDownloadTask {

    private final JmImage image;
    private Path localFilePath;
    private Path tempFilePath;
    private volatile transient Call networkCall;
    private transient OkHttpClient httpClient;
    private final Duration imageTimeout;
    private final DownloadManager downloadManager;

    public ImageDownloadTask(JmImage image, OkHttpClient httpClient, Path localFilePath, Path tempFilePath, Duration imageTimeout, DownloadManager downloadManager) {
        super();
        this.image = image;
        this.httpClient = httpClient;
        this.localFilePath = localFilePath;
        this.tempFilePath = tempFilePath;
        this.imageTimeout = imageTimeout;
        this.downloadManager = downloadManager;
    }

    @Override
    public void start() {
        if (transitState(TaskState.QUEUED, TaskState.RUNNING)) {
            recordStartTimestamp();
            notifyStateChanged(TaskState.RUNNING);

            try {
                this.downloadedBytes = 0;
                this.totalBytes = -1;
                downloadImage(this.image);
                if (transitState(TaskState.RUNNING, TaskState.COMPLETED)) {
                    recordEndTimestamp();
                    DownloadProgress finalPartialProgress = new DownloadProgress(
                            null,
                            null,
                            null,
                            null,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            false,
                            this.downloadedBytes,
                            String.valueOf(System.currentTimeMillis())
                    );
                    notifyProgressUpdate(finalPartialProgress);
                    addSuccessfulFile(this.localFilePath);
                    notifyFinish(getCurrentDownloadResult());
                    notifyStateChanged(TaskState.COMPLETED);
                }
            } catch (Exception e) {
                if (currentState() == TaskState.PAUSED) {
                    // 暂停导致的中断，不算失败
                } else if (currentState() == TaskState.CANCELLING) {
                    // 取消导致的中断，交给 finally 收尾到 CANCELLED
                } else if (currentState() == TaskState.SKIPPED) {
                    // 任务跳过说明之前已完成
                    addSuccessfulFile(this.localFilePath);
                    notifyFinish(getCurrentDownloadResult());
                    notifyStateChanged(TaskState.SKIPPED);
                } else if (transitState(TaskState.RUNNING, TaskState.FAILED)) {
                    recordEndTimestamp();
                    addFailedTask(this.image, e);
                    notifyError(e);
                    notifyStateChanged(TaskState.FAILED);
                }
            } finally {
                if (transitState(TaskState.CANCELLING, TaskState.CANCELLED)) {
                    recordEndTimestamp();
                    notifyStateChanged(TaskState.CANCELLED);
                }

                synchronized (stateLock) {
                    if (this.state.isTerminal()) {
                        // 终态时清理资源
                        this.networkCall = null;
                        this.httpClient = null;
                    }
                }
            }
        }
    }

    @Override
    public void pause() {
        if (transitState(TaskState.QUEUED, TaskState.PAUSED) ||
                transitState(TaskState.RUNNING, TaskState.PAUSED)) {
            notifyStateChanged(TaskState.PAUSED);
            if (this.networkCall != null) {
                // 直接关闭, 不提供断点续传, 恢复时重新下载
                this.networkCall.cancel();
            }
        }
    }

    @Override
    public void resume() {
        if (isState(TaskState.PAUSED)) {
            downloadManager.submit(this);
        }
    }

    @Override
    public void cancel() {
        if (transitState(TaskState.RUNNING, TaskState.CANCELLING) ||
                transitState(TaskState.PENDING, TaskState.CANCELLED) ||
                transitState(TaskState.QUEUED, TaskState.CANCELLED) ||
                transitState(TaskState.PAUSED, TaskState.CANCELLED)) {
            notifyStateChanged(currentState());
            if (this.networkCall != null) {
                this.networkCall.cancel();
            }
        }
    }

    public JmImage getImage() {
        return this.image;
    }

    private byte[] fetchImageBytes(JmImage image) throws IOException {
        Request request = new Request.Builder()
                .url(image.getDownloadUrl())
                .get()
                .build();

        OkHttpClient imageClient = httpClient.newBuilder()
                .readTimeout(this.imageTimeout)
                .build();

        Call currentCall = imageClient.newCall(request);
        this.networkCall = currentCall;

        try (Response response = currentCall.execute()) {

            if (!response.isSuccessful()) {
                throw new ResponseException("Request failed with code: " + response.code());
            }

            okhttp3.ResponseBody body = response.body();
            if (body == null) {
                throw new NetworkException("Response body is null");
            }

            long totalBytes = body.contentLength();
            String encoding = response.header("Content-Encoding");

            try (InputStream raw = body.byteStream();
                 InputStream is = "gzip".equalsIgnoreCase(encoding) ? new GZIPInputStream(raw) : raw) {

                if ("gzip".equalsIgnoreCase(encoding)) {
                    totalBytes = -1;
                }

                this.totalBytes = totalBytes;

                // 初始化内存缓冲区
                ByteArrayOutputStream baos =
                        (totalBytes > 0 && totalBytes <= Integer.MAX_VALUE)
                                ? new ByteArrayOutputStream((int) totalBytes)
                                : new ByteArrayOutputStream(8192);

                // 进度控制与流读取
                byte[] buffer = new byte[8192];
                int len;
                long currentDownloadedBytes = 0;
                long lastNotifiedBytes = 0;
                final long NOTIFY_THRESHOLD = 256 * 1024; // 256KB阈值

                while ((len = is.read(buffer)) != -1) {
                    // 防御性打断检查
                    TaskState state = currentState();
                    if (state == TaskState.PAUSED || state == TaskState.CANCELLING) {
                        throw new IOException("Download manually interrupted by state: " + state.name());
                    }

                    baos.write(buffer, 0, len);
                    currentDownloadedBytes += len;
                    this.downloadedBytes = currentDownloadedBytes;

                    // 判断增量是否达到了 256KB
                    if (currentDownloadedBytes - lastNotifiedBytes >= NOTIFY_THRESHOLD) {
                        lastNotifiedBytes = currentDownloadedBytes;

                        DownloadProgress partialProgress = new DownloadProgress(
                                null,
                                null,
                                null,
                                null,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                false,
                                currentDownloadedBytes,
                                String.valueOf(System.currentTimeMillis())
                        );
                        notifyProgressUpdate(partialProgress);
                    }
                }

                if (currentDownloadedBytes > lastNotifiedBytes) {
                    DownloadProgress finalPartialProgress = new DownloadProgress(
                            null,
                            null,
                            null,
                            null,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            false,
                            currentDownloadedBytes,
                            String.valueOf(System.currentTimeMillis())
                    );
                    notifyProgressUpdate(finalPartialProgress);
                }

                byte[] content = baos.toByteArray();

                if (image.isGif()) {
                    return content;
                }
                return JmImageTool.decryptImage(content, image);
            }
        }
    }

    public void downloadImage(JmImage image) throws IOException {
        if (Files.isDirectory(localFilePath)) {
            // 路径为目录则拼接文件名（净化非法字符）
            localFilePath = localFilePath.resolve(FileUtils.sanitizeFilename(image.filename()));
        }
        // 对路径的最后一级（文件名）统一净化，防止非法字符写入文件系统
        Path parent = localFilePath.getParent();
        String safeFilename = FileUtils.sanitizeFilename(localFilePath.getFileName().toString());
        localFilePath = parent != null ? parent.resolve(safeFilename) : Path.of(safeFilename);

        // 检查文件是否已存在，避免重复下载
        if (Files.exists(localFilePath)) {
            // 尝试清理可能残留的 .tmp 文件（如上次下载在 move 前中断）
            tempFilePath = localFilePath.resolveSibling(localFilePath.getFileName() + ".tmp");
            if (Files.exists(tempFilePath)) {
                try {
                    Files.delete(tempFilePath);
                } catch (IOException ignored) {
                    // 删不掉就算了
                }
            }
            transitState(TaskState.RUNNING, TaskState.SKIPPED);
            throw new IOException("Download skipped");
        }
        byte[] imageBytes = fetchImageBytes(image);
        // 确保路径存在
        if (localFilePath.getParent() != null) {
            Files.createDirectories(localFilePath.getParent());
        }
        /*
         * 先写到 .tmp 再原子重命名，防止下载中断留下残文件。
         * 跨文件系统不支持原子移动时降级为 REPLACE_EXISTING。
         */
        Files.write(tempFilePath, imageBytes);
        try {
            Files.move(tempFilePath, localFilePath, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFilePath, localFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
