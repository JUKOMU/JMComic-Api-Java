package io.github.jukomu.jmcomic.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 创作者作品列表分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmCreatorWorkPage(
        /* 总作品数 */
        int total,
        /* 当前页作品列表 */
        List<JmCreatorWorkMeta> list,
        /* 筛选条件 */
        Map<String, Object> filters
) {

    /**
     * 获取总作品数
     *
     * @return 总作品数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取当前页作品列表
     *
     * @return 作品列表
     */
    public List<JmCreatorWorkMeta> getList() {
        return list;
    }

    /**
     * 获取筛选条件
     *
     * @return 筛选条件
     */
    public Map<String, Object> getFilters() {
        return filters;
    }
}
