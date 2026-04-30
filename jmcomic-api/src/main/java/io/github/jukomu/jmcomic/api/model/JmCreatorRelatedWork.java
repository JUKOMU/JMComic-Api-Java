package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 创作者详情页中的关联作品摘要
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCreatorRelatedWork(
        /* 作品ID */
        String id,
        /* 作品封面图片URL */
        String workImage,
        /* 作品名称 */
        String workTitle,
        /* 作品发布日期描述，如 "25 days ago" */
        String workDate,
        /* 平台名称 */
        String platformName
) {

    /**
     * 获取作品ID
     *
     * @return 作品ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取作品封面图片URL
     *
     * @return 作品封面图片URL
     */
    public String getWorkImage() {
        return workImage;
    }

    /**
     * 获取作品名称
     *
     * @return 作品名称
     */
    public String getWorkTitle() {
        return workTitle;
    }

    /**
     * 获取作品发布日期描述
     *
     * @return 发布日期描述，如 "25 days ago"
     */
    public String getWorkDate() {
        return workDate;
    }

    /**
     * 获取平台名称
     *
     * @return 平台名称
     */
    public String getPlatformName() {
        return platformName;
    }
}
