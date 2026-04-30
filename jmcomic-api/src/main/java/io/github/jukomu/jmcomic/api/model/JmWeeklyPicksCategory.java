package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 每周必看列表中的期数条目
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmWeeklyPicksCategory(
        /* 期数ID */
        String id,
        /* 标题 */
        String title,
        /* 时间范围描述 */
        String time
) {

    /**
     * 获取期数ID
     *
     * @return 期数ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取时间范围描述
     *
     * @return 时间范围描述
     */
    public String getTime() {
        return time;
    }
}
