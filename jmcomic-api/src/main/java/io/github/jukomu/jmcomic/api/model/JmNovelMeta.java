package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 小说列表摘要
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmNovelMeta(
        /* 小说ID */
        String id,
        /* 小说标题 */
        String name,
        /* 作者名称 */
        String author,
        /* 封面图片URL */
        String image,
        /* 当前用户是否已点赞 */
        boolean liked,
        /* 是否已收藏（可能为null） */
        Boolean isFavorite,
        /* 最后一次更新的Unix时间戳（秒） */
        long updateAt,
        /* 点赞数 */
        String likes,
        /* 最新章节索引 */
        String lastChapterIndex,
        /* 最新章节标题 */
        String lastChapterTitle
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
     * 获取当前用户是否已点赞
     *
     * @return 是否已点赞
     */
    public boolean isLiked() {
        return liked;
    }

    /**
     * 获取收藏状态
     *
     * @return 是否已收藏（可能为null）
     */
    public Boolean getIsFavorite() {
        return isFavorite;
    }

    /**
     * 获取最后一次更新的Unix时间戳（秒）
     *
     * @return 更新时间的Unix时间戳
     */
    public long getUpdateAt() {
        return updateAt;
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
}
