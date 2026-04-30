package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 任务列表响应结果
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmTaskList(
        /* 响应消息 */
        String msg,
        /* 响应状态 */
        String status,
        /* 任务列表 */
        List<JmTaskItem> list
) {

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 获取响应状态
     *
     * @return 响应状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取任务列表
     *
     * @return 任务列表
     */
    public List<JmTaskItem> getList() {
        return list;
    }
}
