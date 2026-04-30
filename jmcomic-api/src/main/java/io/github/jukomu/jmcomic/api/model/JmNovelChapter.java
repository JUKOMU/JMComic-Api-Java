package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 小说章节内容（从 getNovelChapter API 返回）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmNovelChapter(
        /* 章节内容ID */
        long ncid,
        /* 所属小说ID */
        String nid,
        /* 章节名称 */
        String name,
        /* 章节标题 */
        String title,
        /* 章节内容的原始HTML（含转义字符） */
        String content,
        /* 章节内容的纯文本（已解码HTML实体并去除标签） */
        String contentText,
        /* 最新章节标题 */
        String lastChapterTitle,
        /* 收藏数 */
        String totalFavorites,
        /* 浏览量 */
        String totalViews,
        /* 点赞数 */
        String totalLikes,
        /* 添加时间 */
        String addtime,
        /* 额外时间字段 */
        String adddt,
        /* 是否已收藏（可能为null） */
        Boolean isFavorite,
        /* 当前用户是否已点赞 */
        boolean liked,
        /* 相关小说列表 */
        List<JmRelatedNovel> relatedList
) {

    /**
     * 获取章节内容ID
     *
     * @return NCID
     */
    public long getNcid() {
        return ncid;
    }

    /**
     * 获取所属小说ID
     *
     * @return 小说ID
     */
    public String getNid() {
        return nid;
    }

    /**
     * 获取章节名称
     *
     * @return 章节名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取章节标题
     *
     * @return 章节标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取章节内容的原始HTML
     *
     * @return 原始HTML内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取章节内容的纯文本（已解码HTML实体并去除标签）
     *
     * @return 纯文本内容
     */
    public String getContentText() {
        return contentText;
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
     * 获取收藏数
     *
     * @return 收藏数字符串
     */
    public String getTotalFavorites() {
        return totalFavorites;
    }

    /**
     * 获取浏览量
     *
     * @return 浏览数字符串
     */
    public String getTotalViews() {
        return totalViews;
    }

    /**
     * 获取点赞数
     *
     * @return 点赞数字符串
     */
    public String getTotalLikes() {
        return totalLikes;
    }

    /**
     * 获取添加时间
     *
     * @return 添加时间字符串
     */
    public String getAddtime() {
        return addtime;
    }

    /**
     * 获取额外时间字段
     *
     * @return 时间字符串
     */
    public String getAdddt() {
        return adddt;
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
     * 获取当前用户是否已点赞
     *
     * @return 是否已点赞
     */
    public boolean isLiked() {
        return liked;
    }

    /**
     * 获取相关小说列表
     *
     * @return 相关小说列表
     */
    public List<JmRelatedNovel> getRelatedList() {
        return relatedList;
    }
}
