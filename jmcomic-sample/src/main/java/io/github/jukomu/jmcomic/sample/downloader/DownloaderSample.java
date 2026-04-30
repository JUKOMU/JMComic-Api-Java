package io.github.jukomu.jmcomic.sample.downloader;

import io.github.jukomu.jmcomic.api.client.DownloadProgress;
import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.client.AbstractJmClient;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;

/**
 * 下载功能示例，展示新旧两种 API 的使用方式。
 *
 * <p>本示例演示三种调用风格：
 * <ol>
 *   <li><b>旧 API</b> — 直接调用 {@code client.downloadAlbum()} / {@code client.downloadPhoto()}，简洁但无进度回调</li>
 *   <li><b>新 API</b> — 通过 {@code client.download(album).withProgress(cb).execute()} 链式调用，支持实时进度</li>
 *   <li><b>高级用法</b> — 自定义保存路径、自定义线程池等</li>
 * </ol>
 *
 * <p>根据需求选择即可，两种 API 完全兼容。
 */
public class DownloaderSample {

    private static final DecimalFormat PCT = new DecimalFormat("0.0%");

    public static void main(String[] args) {
        JmConfiguration config = new JmConfiguration.Builder()
                .clientType(ClientType.API)
                .build();

        try (AbstractJmClient client = JmComic.newApiClient(config)) {

            // ==================== 旧 API - 简洁模式 ====================
            System.out.println("========== 旧 API：直接下载 ==========");
            downloadAlbumSimple(client, "1064000");
            downloadPhotoSimple(client, "540709", 0);

            // ==================== 新 API - 进度回调 ====================
            System.out.println("\n========== 新 API：本子下载 + 进度回调 ==========");
            downloadAlbumWithProgress(client, "1064000");

            System.out.println("\n========== 新 API：章节下载 + 进度回调 ==========");
            downloadPhotoWithProgress(client, "540709", 1);

            // ==================== 高级用法 ====================
            System.out.println("\n========== 高级用法：自定义保存路径 ==========");
            downloadWithCustomPath(client, "1064000");

            System.out.println("\n========== 高级用法：自定义线程池 ==========");
            downloadWithCustomExecutor(client, "1064000");
        }
    }

    // ==================================================================
    // 旧 API — 直接调用，适合不需要进度的场景
    // ==================================================================

    /**
     * 旧 API：下载整个本子。
     * 一行调用，简洁直接，但无法获取实时进度。
     */
    private static void downloadAlbumSimple(AbstractJmClient client, String albumId) {
        JmAlbum album = client.getAlbum(albumId);
        System.out.println("下载本子: " + album.getTitle() + " (" + album.id() + ")");

        DownloadResult result = client.downloadAlbum(album);
        printResultSummary(result);
    }

    /**
     * 旧 API：下载单个章节。
     */
    private static void downloadPhotoSimple(AbstractJmClient client, String albumId, int photoIndex) {
        JmAlbum album = client.getAlbum(albumId);
        JmPhoto photo = client.getPhoto(album.getPhotoMeta(photoIndex).getId());
        System.out.println("下载章节: " + photo.getTitle() + " (" + photo.id() + ")");

        DownloadResult result = client.downloadPhoto(photo);
        printResultSummary(result);
    }

    // ==================================================================
    // 新 API — 链式调用 + 进度回调，适合需要实时反馈的场景
    // ==================================================================

    /**
     * 新 API：本子下载 + 进度回调。
     * 每完成一张图片回调一次，可用于进度条展示。
     */
    private static void downloadAlbumWithProgress(AbstractJmClient client, String albumId) {
        JmAlbum album = client.getAlbum(albumId);
        System.out.println("本子: " + album.getTitle() + " (" + album.id() + ")");

        DownloadResult result = client.download(album)
                .withProgress(DownloaderSample::printAlbumProgress)
                .execute();

        printResultSummary(result);
    }

    /**
     * 新 API：章节下载 + 进度回调。
     */
    private static void downloadPhotoWithProgress(AbstractJmClient client, String albumId, int photoIndex) {
        JmAlbum album = client.getAlbum(albumId);
        JmPhoto photo = client.getPhoto(album.getPhotoMeta(photoIndex).getId());
        System.out.println("章节: " + photo.getTitle() + " (" + photo.id() + ")");

        DownloadResult result = client.download(photo)
                .withProgress(DownloaderSample::printPhotoProgress)
                .execute();

        printResultSummary(result);
    }

    // ==================================================================
    // 高级用法
    // ==================================================================

    /**
     * 新 API：通过 {@code withPath(Path)} 指定自定义保存路径。
     */
    private static void downloadWithCustomPath(AbstractJmClient client, String albumId) {
        Path customDir = Paths.get("downloads", "custom_" + albumId);
        System.out.println("保存到: " + customDir.toAbsolutePath());

        JmAlbum album = client.getAlbum(albumId);
        DownloadResult result = client.download(album)
                .withPath(customDir)
                .withProgress(p -> {
                    if (p.completedImages() % 5 == 0 || p.completedImages() == p.totalImages()) {
                        System.out.printf("  进度: %d/%d 图片, %d/%d 章节%n",
                                p.completedImages(), p.totalImages(),
                                p.completedPhotos(), p.totalPhotos());
                    }
                })
                .execute();

        printResultSummary(result);
    }

    /**
     * 新 API：通过 {@code withExecutor(ExecutorService)} 指定自定义线程池。
     *
     * <p>注意：外部传入的线程池需自行管理生命周期，
     * {@link AbstractJmClient#close()} 不会关闭它。
     */
    private static void downloadWithCustomExecutor(AbstractJmClient client, String albumId) {
        var executor = Executors.newSingleThreadExecutor();
        try {
            JmAlbum album = client.getAlbum(albumId);
            System.out.println("线程池: 单线程（串行下载）  本子: " + album.getTitle());

            DownloadResult result = client.download(album)
                    .withExecutor(executor)
                    .withProgress(p -> System.out.printf("  [串行] %d/%d 图片%n",
                            p.completedImages(), p.totalImages()))
                    .execute();

            printResultSummary(result);
        } finally {
            executor.shutdown();
        }
    }

    // ==================================================================
    // 共用方法
    // ==================================================================

    /**
     * 本子级进度回调：打印图片和章节完成情况。
     */
    private static void printAlbumProgress(DownloadProgress p) {
        System.out.printf("  本子进度: 图片 %d/%d (%s), 章节 %d/%d (%s)%n",
                p.completedImages(), p.totalImages(), formatPct(p.completedImages(), p.totalImages()),
                p.completedPhotos(), p.totalPhotos(), formatPct(p.completedPhotos(), p.totalPhotos()));
    }

    /**
     * 章节级进度回调：打印图片完成情况（附带本子标题，演示 albumTitle 字段）。
     */
    private static void printPhotoProgress(DownloadProgress p) {
        System.out.printf("  章节 '%s' (本子: %s) 进度: %d/%d (%s)%n",
                p.photoTitle(), p.albumTitle(),
                p.completedImages(), p.totalImages(),
                formatPct(p.completedImages(), p.totalImages()));
    }

    /**
     * 打印下载结果摘要。
     */
    private static void printResultSummary(DownloadResult result) {
        if (result.isAllSuccess()) {
            System.out.println("  ✅ 完成，共 " + result.getSuccessfulFiles().size() + " 张图片");
        } else {
            System.out.println("  ⚠️ 部分失败：成功 " + result.getSuccessfulFiles().size()
                    + "，失败 " + result.getFailedTasks().size());
            result.getFailedTasks().forEach((image, error) ->
                    System.err.println("    ✗ " + image.getTag() + ": " + error.getMessage()));
        }
    }

    private static String formatPct(int part, int total) {
        if (total <= 0) return "N/A";
        return PCT.format((double) part / total);
    }
}
