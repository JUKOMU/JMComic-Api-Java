package io.github.jukomu.jmcomic.api.enums;

/**
 * 论坛/评论区的内容分类模式
 */
public enum ForumMode {
    ALL("all", "显示全部"),
    MANHUA("manhua", "漫画评论"),
    CHAT("chat", "闲聊大厅");

    private final String value;
    private final String description;

    ForumMode(String value, String description) {
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
