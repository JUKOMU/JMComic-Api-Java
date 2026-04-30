package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 创作者/作者摘要
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmCreatorMeta(
        /* 创作者ID */
        String id,
        /* 创作者名称 */
        String name,
        /* 背景图片URL */
        String backgroundImage,
        /* 最后更新日期的描述文本，如 "25 days ago" */
        String updateDate,
        /* 头像图片URL */
        String avatarImage
) {

    /**
     * 获取创作者ID
     *
     * @return 创作者ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取创作者名称
     *
     * @return 创作者名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取背景图片URL
     *
     * @return 背景图片URL
     */
    public String getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * 获取最后更新日期的描述文本
     *
     * @return 更新日期文本，如 "25 days ago"
     */
    public String getUpdateDate() {
        return updateDate;
    }

    /**
     * 获取头像图片URL
     *
     * @return 头像图片URL
     */
    public String getAvatarImage() {
        return avatarImage;
    }
}
