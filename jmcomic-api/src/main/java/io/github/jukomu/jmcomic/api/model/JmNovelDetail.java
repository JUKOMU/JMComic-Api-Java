package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 小说详情
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmNovelDetail(
        /* 小说ID */
        String id,
        /* 系列ID */
        String seriesId,
        /* 小说标题 */
        String name,
        /* 封面图片URL */
        String images,
        /* 添加时间的Unix时间戳（秒） */
        long addtime,
        /* 小说描述 */
        String description,
        /* 总浏览量 */
        String totalViews,
        /* 点赞数 */
        String likes,
        /* 是否完结（"0"=未完结, "1"=已完结） */
        String isEnd,
        /* 连载状态 */
        String serialStatus,
        /* 作者名称 */
        String author,
        /* 标签列表 */
        List<String> tags,
        /* 当前用户是否已点赞 */
        boolean liked,
        /* 是否已收藏（可能为null） */
        Boolean isFavorite,
        /* 章节列表 */
        List<JmNovelChapterMeta> series,
        /* 相关小说列表 */
        List<JmRelatedNovel> relatedList,
        /* 评论列表 */
        List<JmNovelComment> commentTotal
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
     * 获取系列ID
     *
     * @return 系列ID
     */
    public String getSeriesId() {
        return seriesId;
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
     * 获取封面图片URL
     *
     * @return 封面图片URL
     */
    public String getImages() {
        return images;
    }

    /**
     * 获取添加时间的Unix时间戳（秒）
     *
     * @return 添加时间
     */
    public long getAddtime() {
        return addtime;
    }

    /**
     * 获取小说描述
     *
     * @return 小说描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取总浏览量
     *
     * @return 总浏览量
     */
    public String getTotalViews() {
        return totalViews;
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
     * 获取是否完结
     *
     * @return 是否完结（"0"/"1"）
     */
    public String getIsEnd() {
        return isEnd;
    }

    /**
     * 获取连载状态
     *
     * @return 连载状态
     */
    public String getSerialStatus() {
        return serialStatus;
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
     * 获取标签列表
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        return tags;
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
     * 获取章节列表
     *
     * @return 章节列表
     */
    public List<JmNovelChapterMeta> getSeries() {
        return series;
    }

    /**
     * 获取相关小说列表
     *
     * @return 相关小说列表
     */
    public List<JmRelatedNovel> getRelatedList() {
        return relatedList;
    }

    /**
     * 获取评论列表
     *
     * @return 评论列表
     */
    public List<JmNovelComment> getCommentTotal() {
        return commentTotal;
    }
}
