package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 小说详情中的相关小说条目（related_list 字段）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmRelatedNovel(
        /* 小说ID */
        String id,
        /* 小说标题 */
        String name,
        /* 作者名称 */
        String author,
        /* 封面图片URL */
        String image,
        /* 章节更新的Unix时间戳（秒） */
        long chapterUpdateAt,
        /* 更新时间 */
        String updateAt,
        /* 最新章节索引 */
        String lastChapterIndex,
        /* 最新章节标题 */
        String lastChapterTitle,
        /* 点赞数 */
        String likes,
        /* 是否已收藏（可能为null） */
        Boolean isFavorite
) {

    /**
     * 获取小说ID
     *
     * @return 小说ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取小说标题
     *
     * @return 小说标题
     */
    public String getName() {
        return name;
    }

    /**
     * 获取作者名称
     *
     * @return 作者名称
     */
    public String getAuthor() {
        return author;
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
     * 获取章节更新的Unix时间戳（秒）
     *
     * @return 章节更新时间戳
     */
    public long getChapterUpdateAt() {
        return chapterUpdateAt;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public String getUpdateAt() {
        return updateAt;
    }

    /**
     * 获取最新章节索引
     *
     * @return 最新章节索引
     */
    public String getLastChapterIndex() {
        return lastChapterIndex;
    }

    /**
     * 获取最新章节标题
     *
     * @return 最新章节标题
     */
    public String getLastChapterTitle() {
        return lastChapterTitle;
    }

    /**
     * 获取点赞数
     *
     * @return 点赞数
     */
    public String getLikes() {
        return likes;
    }

    /**
     * 获取收藏状态
     *
     * @return 是否已收藏（可能为null）
     */
    public Boolean getIsFavorite() {
        return isFavorite;
    }
}
