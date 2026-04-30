package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 收藏标签
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmTagFavorite(
        /* 标签名称 */
        String tag,
        /* 更新时间的文字描述（如 "33 秒 ago"） */
        String updatedAt
) {

    /**
     * 获取标签名称
     *
     * @return 标签名称
     */
    public String getTag() {
        return tag;
    }

    /**
     * 获取更新时间的文字描述
     *
     * @return 更新时间的文字描述（如 "33 秒 ago"）
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
}
