package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 创作者作品信息（作品弹窗详情）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCreatorWorkInfo(
        /* 作品发布日期描述，如 "20569 days ago" */
        String workDate,
        /* 创作者名称 */
        String authorName,
        /* 作品名称 */
        String workTitle,
        /* 关联作品列表 */
        List<JmCreatorRelatedWork> relatedWorks
) {

    /**
     * 获取作品发布日期描述
     *
     * @return 发布日期描述，如 "20569 days ago"
     */
    public String getWorkDate() {
        return workDate;
    }

    /**
     * 获取创作者名称
     *
     * @return 创作者名称
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * 获取作品名称
     *
     * @return 作品名称
     */
    public String getWorkTitle() {
        return workTitle;
    }

    /**
     * 获取关联作品列表
     *
     * @return 关联作品列表
     */
    public List<JmCreatorRelatedWork> getRelatedWorks() {
        return relatedWorks;
    }
}
