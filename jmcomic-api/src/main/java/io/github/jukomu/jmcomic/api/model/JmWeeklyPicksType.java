package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 每周必看类型（如韩漫、日漫、其他）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmWeeklyPicksType(
        /* 类型ID */
        String id,
        /* 类型名称 */
        String title
) {

    /**
     * 获取类型ID
     *
     * @return 类型ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTitle() {
        return title;
    }
}
