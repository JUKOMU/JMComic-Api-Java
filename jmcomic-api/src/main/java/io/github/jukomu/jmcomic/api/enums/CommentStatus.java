package io.github.jukomu.jmcomic.api.enums;

/**
 * 评论状态（是否有剧透）
 */
public enum CommentStatus {
    NORMAL("", "无剧透"),
    SPOILER("true", "有剧透");

    private final String value;
    private final String description;

    CommentStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
