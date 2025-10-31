package io.github.jukomu.jmcomic.api.model;

import java.util.Objects;

/**
 * @author JUKOMU
 * @Description: 代表一张单独的、可下载的图片信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmImage(
        /*
          该图片所属的章节(Photo)的ID
         */
        String photoId,
        /*
          用于图片解密的 scramble ID
         */
        String scrambleId,
        /*
          图片的完整文件名，包含后缀 (例如 "00001.webp")
         */
        String filename,
        /*
          图片的CDN URL，不包含查询参数
         */
        String url,
        /*
          图片URL的查询参数 (例如 "v=1697541064")
         */
        String queryParams,
        /*
          图片在本章节中的顺序，从1开始
         */
        int sortOrder
) {
    /**
     * 获取不带后缀的文件名
     *
     * @return 例如 "00001"
     */
    public String getFilenameWithoutSuffix() {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    /**
     * 判断图片是否为GIF格式
     *
     * @return 如果是.gif则为true，否则为false
     */
    public boolean isGif() {
        return filename.toLowerCase().endsWith(".gif");
    }

    /**
     * 获取完整的、可用于下载的图片URL（包含查询参数）
     *
     * @return 完整的图片下载链接
     */
    public String getDownloadUrl() {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        return url + "?" + queryParams;
    }

    /**
     * 获取用于日志输出的、易于识别的标签
     *
     * @return 格式为 "photoId/filename [sortOrder]"
     */
    public String getTag() {
        return String.format("%s/%s [%d]", photoId, filename, sortOrder);
    }

    // 重写 record 自动生成的 equals 和 hashCode，确保 queryParams 为 null 和为空字符串时被视为相等
    // 这对于将对象作为 Map 的 key 或放入 Set 中非常重要
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JmImage jmImage = (JmImage) o;
        return sortOrder == jmImage.sortOrder &&
                photoId.equals(jmImage.photoId) &&
                scrambleId.equals(jmImage.scrambleId) &&
                filename.equals(jmImage.filename) &&
                url.equals(jmImage.url) &&
                Objects.equals(queryParams == null ? "" : queryParams, jmImage.queryParams == null ? "" : jmImage.queryParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoId, scrambleId, filename, url, queryParams == null ? "" : queryParams, sortOrder);
    }
}
