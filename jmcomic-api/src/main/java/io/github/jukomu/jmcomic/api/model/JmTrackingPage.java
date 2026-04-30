package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 连载跟踪列表分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmTrackingPage(
        /* 总跟踪数（字符串类型，如 "2"） */
        String totalCnt,
        /* 当前页跟踪列表 */
        List<JmTrackingItem> item
) {

    /**
     * 获取总跟踪数
     *
     * @return 总跟踪数
     */
    public String getTotalCnt() {
        return totalCnt;
    }

    /**
     * 获取当前页跟踪列表
     *
     * @return 跟踪列表
     */
    public List<JmTrackingItem> getItem() {
        return item;
    }
}
