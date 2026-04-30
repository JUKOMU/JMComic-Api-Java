package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 创作者列表分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmCreatorPage(
        /* 总创作者数 */
        int total,
        /* 当前页创作者列表 */
        List<JmCreatorMeta> list
) {

    /**
     * 获取总创作者数
     *
     * @return 总创作者数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取当前页创作者列表
     *
     * @return 创作者列表
     */
    public List<JmCreatorMeta> getList() {
        return list;
    }
}
