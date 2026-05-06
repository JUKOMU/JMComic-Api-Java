package io.github.jukomu.jmcomic.api.download;

/**
 * 下载进度的值对象，通过 {@link DownloadRequest#withProgress(java.util.function.Consumer)} 注册回调后，
 * 在下载过程中每完成一张图片或一个章节时回调。
 *
 * @param albumId         本子ID
 * @param albumTitle      本子标题
 * @param photoId         章节ID（章节级进度有效）
 * @param photoTitle      章节标题（章节级进度有效）
 * @param completedImages 已结束且视为完成的图片数，包含成功/跳过/部分成功
 * @param FailedImages    已结束但未完成的图片数，包含失败/取消
 * @param totalImages     总图片数
 * @param completedPhotos 已结束且视为完成的章节数，包含成功/跳过/部分成功
 * @param FailedPhotos    已结束但未完成的章节数，包含失败/取消
 * @param totalPhotos     总章节数（本子级进度有效）
 * @param isAlbumLevel    是本子级下载还是章节级下载
 * @param downloadedBytes 已下载的总字节数，用于下载速度/处理速度统计
 * @param createTimestamp 本次进度快照创建时间戳
 */
public record DownloadProgress(
        String albumId,
        String albumTitle,
        String photoId,
        String photoTitle,
        int completedImages,
        int FailedImages,
        int totalImages,
        int completedPhotos,
        int FailedPhotos,
        int totalPhotos,
        boolean isAlbumLevel,
        long downloadedBytes,
        String createTimestamp
) {
}
