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
}
