package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 代表一条系统通知/消息，
 * 通常出现在通知列表的分页结果中
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmNotification(
        /*
          通知ID
         */
        String id,
        /*
          通知标题
         */
        String title,
        /*
          通知正文内容
         */
        String content,
        /*
          通知发送时间
         */
        String time,
        /*
          是否已读
         */
        boolean read,
        /*
          通知类型（如 comic_follow, site_notice 等）
         */
        String type
) {

    /**
     * 获取通知ID
     *
     * @return 通知ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取通知标题
     *
     * @return 通知标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取通知正文内容
     *
     * @return 通知内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取通知发送时间
     *
     * @return 发送时间
     */
    public String getTime() {
        return time;
    }

    /**
     * 获取是否已读
     *
     * @return 是否已读
     */
    public boolean isRead() {
        return read;
    }

    /**
     * 获取通知类型
     *
     * @return 通知类型
     */
    public String getType() {
        return type;
    }
}
