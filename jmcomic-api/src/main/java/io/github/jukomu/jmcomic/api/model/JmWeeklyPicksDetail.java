package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表每周必看筛选的一页结果（{total, list}）
 * @Project: jmcomic-api-java
 * @Date: 2026/04/28
 */
public record JmWeeklyPicksDetail(
        /*
          结果总数
         */
        int total,
        /*
          本子摘要信息列表
         */
        List<JmAlbumMeta> list
) {

    /**
     * 获取结果总数
     *
     * @return 结果总数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取本子摘要信息列表
     *
     * @return 本子摘要列表
     */
    public List<JmAlbumMeta> getList() {
        return list;
    }
}
