package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 定义搜索和分类时的时间范围选项
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public enum TimeOption {
    /**
     * 今日
     */
    TODAY("t"),
    /**
     * 本周
     */
    WEEK("w"),
    /**
     * 本月
     */
    MONTH("m"),
    /**
     * 全部时间
     */
    ALL("a");

    private final String value;

    TimeOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
