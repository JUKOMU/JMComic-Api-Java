package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 签到记录中的单天数据
 * @Project: jmcomic-api-java
 * @Date: 2026/4/28
 */
public record JmDailyCheckInRecordItem(
        /* 日期（如 "01"） */
        String date,
        /* 是否已签到（可 null，表示未来日期） */
        Boolean signed,
        /* 是否为奖励日 */
        boolean bonus
) {

    /**
     * 获取日期
     *
     * @return 日期字符串
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取签到状态
     *
     * @return true 已签到，false/null 未签到
     */
    public Boolean getSigned() {
        return signed;
    }

    /**
     * 判断是否为奖励日
     *
     * @return true 奖励日，false 非奖励日
     */
    public boolean isBonus() {
        return bonus;
    }
}
