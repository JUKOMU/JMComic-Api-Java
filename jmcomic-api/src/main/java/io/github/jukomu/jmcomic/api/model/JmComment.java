package io.github.jukomu.jmcomic.api.model;

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
          评论的文本内容。
         */
        String content,
        /*
          评论发布的时间戳或日期字符串。
         */
        String postDate
) {

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
     * 获取评论的文本内容
     *
     * @return 评论内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取评论发布的时间戳或日期字符串
     *
     * @return 发布日期
     */
    public String getPostDate() {
        return postDate;
    }
}
