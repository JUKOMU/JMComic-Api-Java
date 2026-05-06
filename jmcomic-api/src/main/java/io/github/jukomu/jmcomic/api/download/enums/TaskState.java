package io.github.jukomu.jmcomic.api.download.enums;

/**
 * 下载任务状态枚举。
 */
public enum TaskState {
    /* 刚创建、尚未入队 */
    PENDING,
    /* 已入队等待调度 */
    QUEUED,
    /* 正在下载 */
    RUNNING,
    /* 已暂停 */
    PAUSED,
    /* 正在取消中 */
    CANCELLING,
    /* 已取消（终态，不可恢复） */
    CANCELLED,
    /* 全部成功（终态） */
    COMPLETED,
    /* 部分成功（终态） */
    COMPLETED_WITH_ERRORS,
    /* 全部失败（终态） */
    FAILED,
    /* 跳过（终态） */
    SKIPPED;

    /**
     * 是否终态（不可再切换为非终态）
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS
                || this == FAILED || this == CANCELLED || this == SKIPPED;
    }

    /**
     * 是否活跃状态（占用下载资源或即将占用）
     */
    public boolean isActive() {
        return this == QUEUED || this == RUNNING;
    }
}
