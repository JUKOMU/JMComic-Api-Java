package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 本子下载信息（getAlbumDownloadInfo 返回值）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmAlbumDownloadInfo(
        /* 本子标题 */
        String title,
        /* 文件大小，如 "3.9 MB" */
        String fileSize,
        /* 下载链接 */
        String downloadUrl,
        /* 封面图链接 */
        String imgUrl
) {

    /**
     * 获取本子标题
     *
     * @return 本子标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public String getFileSize() {
        return fileSize;
    }

    /**
     * 获取下载链接
     *
     * @return 下载链接
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * 获取封面图链接
     *
     * @return 封面图链接
     */
    public String getImgUrl() {
        return imgUrl;
    }
}
