package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 小说详情中的评论条目（comment_total 字段）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmNovelComment(
        /* 评论ID */
        String commentId,
        /* 关联的小说ID */
        String novelId,
        /* 关联的章节ID（0 表示对小说的评论） */
        String chapterId,
        /* 用户ID */
        String userId,
        /* 评论内容文本 */
        String comment,
        /* 评论发布的Unix时间戳（秒） */
        long addtime,
        /* 点赞数字符串 */
        String likes,
        /* 状态 */
        String status,
        /* 用户名 */
        String username,
        /* 用户昵称 */
        String nickname,
        /* 用户头像URL */
        String photo,
        /* 用户性别 */
        String gender,
        /* 更新时间 */
        String updateAt,
        /* 置顶标记 */
        String pinning,
        /* 评论的HTML内容 */
        String contentHtml,
        /* 剧透标记 */
        String spoiler
) {

    /**
     * 获取评论ID
     *
     * @return 评论ID
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * 获取关联的小说ID
     *
     * @return 小说ID
     */
    public String getNovelId() {
        return novelId;
    }

    /**
     * 获取关联的章节ID
     *
     * @return 章节ID（0 表示对小说的评论）
     */
    public String getChapterId() {
        return chapterId;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取评论内容文本
     *
     * @return 评论内容
     */
    public String getComment() {
        return comment;
    }

    /**
     * 获取评论发布的Unix时间戳（秒）
     *
     * @return 发布时间戳
     */
    public long getAddtime() {
        return addtime;
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
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取用户昵称
     *
     * @return 用户昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取用户头像URL
     *
     * @return 头像URL
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * 获取用户性别
     *
     * @return 性别
     */
    public String getGender() {
        return gender;
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
     * 获取置顶标记
     *
     * @return 置顶标记
     */
    public String getPinning() {
        return pinning;
    }

    /**
     * 获取评论的HTML内容
     *
     * @return HTML内容
     */
    public String getContentHtml() {
        return contentHtml;
    }

    /**
     * 获取剧透标记
     *
     * @return 剧透标记
     */
    public String getSpoiler() {
        return spoiler;
    }
}
