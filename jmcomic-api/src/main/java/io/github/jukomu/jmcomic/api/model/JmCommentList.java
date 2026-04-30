package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表评论列表的一页结果
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmCommentList(
        /*
          评论总数。
         */
        int total,
        /*
          当前页的评论列表。
         */
        List<JmComment> list
) {

    /**
     * 获取评论总数
     *
     * @return 评论总数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取评论列表
     *
     * @return 评论列表
     */
    public List<JmComment> getList() {
        return list;
    }
}
