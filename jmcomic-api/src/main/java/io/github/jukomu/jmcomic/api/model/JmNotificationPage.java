package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 通知列表分页结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
public record JmNotificationPage(
        /* 总通知数 */
        int total,
        /* 当前页通知列表 */
        List<JmNotification> list
) {

    /**
     * 获取总通知数
     *
     * @return 总通知数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 获取当前页通知列表
     *
     * @return 通知列表
     */
    public List<JmNotification> getList() {
        return list;
    }
}
