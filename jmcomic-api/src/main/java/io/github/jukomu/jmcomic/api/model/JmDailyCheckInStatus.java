package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 每日签到状态响应
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmDailyCheckInStatus(
        /* 签到活动ID */
        int dailyId,
        /* 三天签到总金币 */
        String threeDaysCoin,
        /* 三天签到总经验 */
        String threeDaysExp,
        /* 七天签到总金币 */
        String sevenDaysCoin,
        /* 七天签到总经验 */
        String sevenDaysExp,
        /* 活动名称 */
        String eventName,
        /* PC背景图URL */
        String backgroundPc,
        /* 手机背景图URL */
        String backgroundPhone,
        /* 当前进度（如 "0%"） */
        String currentProgress,
        /* 签到记录（周 × 天） */
        List<List<JmDailyCheckInRecordItem>> record
) {

    /**
     * 获取签到活动ID
     *
     * @return 签到活动ID
     */
    public int getDailyId() {
        return dailyId;
    }

    /**
     * 获取三天签到总金币
     *
     * @return 三天签到总金币
     */
    public String getThreeDaysCoin() {
        return threeDaysCoin;
    }

    /**
     * 获取三天签到总经验
     *
     * @return 三天签到总经验
     */
    public String getThreeDaysExp() {
        return threeDaysExp;
    }

    /**
     * 获取七天签到总金币
     *
     * @return 七天签到总金币
     */
    public String getSevenDaysCoin() {
        return sevenDaysCoin;
    }

    /**
     * 获取七天签到总经验
     *
     * @return 七天签到总经验
     */
    public String getSevenDaysExp() {
        return sevenDaysExp;
    }

    /**
     * 获取活动名称
     *
     * @return 活动名称
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * 获取PC背景图URL
     *
     * @return PC背景图URL
     */
    public String getBackgroundPc() {
        return backgroundPc;
    }

    /**
     * 获取手机背景图URL
     *
     * @return 手机背景图URL
     */
    public String getBackgroundPhone() {
        return backgroundPhone;
    }

    /**
     * 获取当前进度
     *
     * @return 当前进度
     */
    public String getCurrentProgress() {
        return currentProgress;
    }

    /**
     * 获取签到记录（周 × 天）
     *
     * @return 签到记录二维列表
     */
    public List<List<JmDailyCheckInRecordItem>> getRecord() {
        return record;
    }
}
