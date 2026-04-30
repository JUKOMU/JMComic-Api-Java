package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 连载跟踪列表中的单个条目
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmTrackingItem(
        /* 本子ID */
        String id,
        /* 本子名称 */
        String name,
        /* 封面图片URL */
        String image,
        /* 更新时间戳（Unix秒） */
        String updateAt
) {

    /**
     * 获取本子ID
     *
     * @return 本子ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取本子名称
     *
     * @return 本子名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取封面图片URL
     *
     * @return 封面图片URL
     */
    public String getImage() {
        return image;
    }

    /**
     * 获取更新时间戳
     *
     * @return 更新时间戳（Unix秒）
     */
    public String getUpdateAt() {
        return updateAt;
    }
}
