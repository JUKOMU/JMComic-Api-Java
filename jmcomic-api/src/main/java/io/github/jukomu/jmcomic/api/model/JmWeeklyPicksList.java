package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 每周必看列表响应结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmWeeklyPicksList(
        /* 期数列表 */
        List<JmWeeklyPicksCategory> categories,
        /* 类型列表 */
        List<JmWeeklyPicksType> type
) {

    /**
     * 获取期数列表
     *
     * @return 期数列表
     */
    public List<JmWeeklyPicksCategory> getCategories() {
        return categories;
    }

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    public List<JmWeeklyPicksType> getType() {
        return type;
    }
}
