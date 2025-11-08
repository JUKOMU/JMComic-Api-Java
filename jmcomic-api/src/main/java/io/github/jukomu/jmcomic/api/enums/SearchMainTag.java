package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 搜索的主标签/搜索类型
 * @Project: jmcomic-api-java
 * @Date: 2025/11/9
 */
public enum SearchMainTag {
    SITE_SEARCH(0, "站内搜索"),
    WORK(1, "作品"),
    AUTHOR(2, "作者"),
    TAG(3, "标签"),
    ACTOR(4, "登场人物");

    private int value;
    private String description;

    SearchMainTag(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
