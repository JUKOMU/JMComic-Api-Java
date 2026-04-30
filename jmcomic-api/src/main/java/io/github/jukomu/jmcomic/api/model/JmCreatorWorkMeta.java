package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 创作者作品摘要
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmCreatorWorkMeta(
        /* 作品ID */
        String id,
        /* 作品名称 */
        String name,
        /* 作品封面图片URL */
        String workImage,
        /* 平台名称 */
        String platformName,
        /* 作品发布日期，如 "2026-04-01 16:14:49" */
        String workDate,
        /* 创作者名称 */
        String authorName,
        /* 创作者ID */
        String authorId
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
     * 获取作品名称
     *
     * @return 作品名称
     */
    public String getName() {
        return name;
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
     * 获取平台名称
     *
     * @return 平台名称
     */
    public String getPlatformName() {
        return platformName;
    }

    /**
     * 获取作品发布日期
     *
     * @return 发布日期，如 "2026-04-01 16:14:49"
     */
    public String getWorkDate() {
        return workDate;
    }

    /**
     * 获取创作者名称
     *
     * @return 创作者名称
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * 获取创作者ID
     *
     * @return 创作者ID
     */
    public String getAuthorId() {
        return authorId;
    }
}
