package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 任务列表中的单个任务条目
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmTaskItem(
        /* 任务ID */
        String id,
        /* 任务名称 */
        String name,
        /* 任务类型（如 title） */
        String type,
        /* 任务头衔内容 */
        String content,
        /* 奖励金币数 */
        String coin,
        /* 任务规则（JSON字符串） */
        String rule,
        /* 开始时间 */
        String beginTime,
        /* 结束时间 */
        String endTime,
        /* 创建时间 */
        String createdAt,
        /* 更新时间（可空） */
        String updatedAt,
        /* 是否已完成 */
        boolean done
) {

    /**
     * 获取任务ID
     *
     * @return 任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取任务类型
     *
     * @return 任务类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取头衔内容
     *
     * @return 头衔内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取奖励金币数
     *
     * @return 金币数
     */
    public String getCoin() {
        return coin;
    }

    /**
     * 获取任务规则
     *
     * @return 任务规则（JSON字符串）
     */
    public String getRule() {
        return rule;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public String getBeginTime() {
        return beginTime;
    }

    /**
     * 获取结束时间
     *
     * @return 结束时间
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间（可空）
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 判断任务是否已完成
     *
     * @return true 已完成，false 未完成
     */
    public boolean isDone() {
        return done;
    }
}
