package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 小说列表分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmNovelPage(
        /* 总条目数 */
        int total,
        /* 当前页小说列表 */
        List<JmNovelMeta> list
) {

    /**
     * 获取总条目数
     *
     * @return 总条目数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取当前页小说列表
     *
     * @return 小说列表
     */
    public List<JmNovelMeta> getList() {
        return list;
    }
}
