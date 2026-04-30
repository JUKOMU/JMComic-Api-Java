package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表一条用户评论
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmComment(
        /*
          评论的唯一ID。
         */
        String commentId,
        /*
          发表评论的用户的ID。
         */
        String userId,
        /*
          发表评论的用户的用户名。
         */
        String username,
        /*
          发表评论的用户昵称。
         */
        String nickname,
        /*
          评论的纯文本内容。
         */
        String content,
        /*
          评论的原始 HTML 内容。
         */
        String contentHtml,
        /*
          评论发布的时间戳或日期字符串。
         */
        String postDate,
        /*
          用户头像URL。
         */
        String photo,
        /*
          用户等级信息 (如 "Lv.5")。
         */
        String expinfo,
        /*
          用户等级信息的结构化数据。
         */
        JmCommentExpInfo expinfoData,
        /*
          关联的专辑/本子ID。
         */
        String aid,
        /*
          关联的博客ID。
         */
        String bid,
        /*
          关联的小说ID。
         */
        String nid,
        /*
          关联的小说评论ID。
         */
        String ncid,
        /*
          名称前缀 (如 "JM12345")。
         */
        String name,
        /*
          评论中的点赞数字段。
         */
        int likes,
        /*
          性别。
         */
        String gender,
        /*
          更新时间戳。
         */
        String updateAt,
        /*
          父评论ID。
         */
        String parentCommentId,
        /*
          剧透标记。
         */
        String spoiler,
        /*
          该评论下的回复列表。
         */
        List<JmComment> replys,
        /*
          点赞数。
         */
        int voteUp,
        /*
          点踩数。
         */
        int voteDown
) {

    public JmComment(
            String commentId,
            String userId,
            String username,
            String content,
            String postDate,
            String photo,
            String expinfo,
            String aid,
            String name,
            List<JmComment> replys,
            int voteUp,
            int voteDown
    ) {
        this(
                commentId,
                userId,
                username,
                "",
                content,
                content,
                postDate,
                photo,
                expinfo,
                JmCommentExpInfo.empty(expinfo),
                aid,
                "",
                "",
                "",
                name,
                0,
                "",
                "",
                "",
                "",
                replys,
                voteUp,
                voteDown
        );
    }

    /**
     * 获取评论的唯一ID
     *
     * @return 评论ID
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * 获取发表评论的用户的ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取发表评论的用户的用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取发表评论的用户昵称
     *
     * @return 用户昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取评论的文本内容
     *
     * @return 评论内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取评论的原始 HTML 内容
     *
     * @return HTML 评论内容
     */
    public String getContentHtml() {
        return contentHtml;
    }

    /**
     * 获取评论发布的时间戳或日期字符串
     *
     * @return 发布日期
     */
    public String getPostDate() {
        return postDate;
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
     * 获取用户等级信息
     *
     * @return 等级信息
     */
    public String getExpinfo() {
        return expinfo;
    }

    /**
     * 获取结构化等级信息
     *
     * @return 结构化等级信息
     */
    public JmCommentExpInfo getExpinfoData() {
        return expinfoData;
    }

    /**
     * 获取关联的专辑ID
     *
     * @return 专辑ID
     */
    public String getAid() {
        return aid;
    }

    /**
     * 获取关联的博客ID
     *
     * @return 博客ID
     */
    public String getBid() {
        return bid;
    }

    /**
     * 获取关联的小说ID
     *
     * @return 小说ID
     */
    public String getNid() {
        return nid;
    }

    /**
     * 获取关联的小说评论ID
     *
     * @return 小说评论ID
     */
    public String getNcid() {
        return ncid;
    }

    /**
     * 获取名称前缀
     *
     * @return 名称前缀
     */
    public String getName() {
        return name;
    }

    /**
     * 获取点赞数字段
     *
     * @return 点赞数字段
     */
    public int getLikes() {
        return likes;
    }

    /**
     * 获取性别
     *
     * @return 性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 获取更新时间戳
     *
     * @return 更新时间戳
     */
    public String getUpdateAt() {
        return updateAt;
    }

    /**
     * 获取父评论ID
     *
     * @return 父评论ID
     */
    public String getParentCommentId() {
        return parentCommentId;
    }

    /**
     * 获取剧透标记
     *
     * @return 剧透标记
     */
    public String getSpoiler() {
        return spoiler;
    }

    /**
     * 获取回复列表
     *
     * @return 回复评论列表
     */
    public List<JmComment> getReplys() {
        return replys;
    }

    /**
     * 获取点赞数
     *
     * @return 点赞数
     */
    public int getVoteUp() {
        return voteUp;
    }

    /**
     * 获取点踩数
     *
     * @return 点踩数
     */
    public int getVoteDown() {
        return voteDown;
    }
}
