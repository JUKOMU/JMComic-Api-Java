package io.github.jukomu.jmcomic.api.client;

/**
 * 下载进度的值对象，通过 {@link DownloadRequest#withProgress(java.util.function.Consumer)} 注册回调后，
 * 在下载过程中每完成一张图片或一个章节时回调。
 *
 * @param albumId         本子ID
 * @param albumTitle      本子标题
 * @param photoId         章节ID（章节级进度有效）
 * @param photoTitle      章节标题（章节级进度有效）
 * @param completedImages 已完成图片数
 * @param totalImages     总图片数
 * @param completedPhotos 已完成章节数（本子级进度有效）
 * @param totalPhotos     总章节数（本子级进度有效）
 * @param isAlbumLevel    是本子级下载还是章节级下载
 */
public record DownloadProgress(
        String albumId,
        String albumTitle,
        String photoId,
        String photoTitle,
        int completedImages,
        int totalImages,
        int completedPhotos,
        int totalPhotos,
        boolean isAlbumLevel
) {
}
