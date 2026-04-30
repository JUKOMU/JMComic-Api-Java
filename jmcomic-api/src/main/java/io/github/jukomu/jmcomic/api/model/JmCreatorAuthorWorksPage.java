package io.github.jukomu.jmcomic.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author JUKOMU
 * @Description: 创作者个人作品列表页（作者详情 + 关联作品）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCreatorAuthorWorksPage(
        /* 作品标题（该字段通常为空） */
        String workTitle,
        /* 创作者名称 */
        String authorName,
        /* 作品日期（该字段通常为空） */
        String workDate,
        /* 创作者头像URL */
        String authorAvatar,
        /* 背景图片URL */
        String backgroundImage,
        /* 赞助平台列表 */
        List<JmCreatorSponsor> sponsors,
        /* 关联作品列表 */
        List<JmCreatorRelatedWork> relatedWorks,
        /* 筛选条件 */
        Map<String, Object> filters
) {

    /**
     * 获取作品标题
     *
     * @return 作品标题
     */
    public String getWorkTitle() {
        return workTitle;
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
     * 获取作品日期
     *
     * @return 作品日期
     */
    public String getWorkDate() {
        return workDate;
    }

    /**
     * 获取创作者头像URL
     *
     * @return 创作者头像URL
     */
    public String getAuthorAvatar() {
        return authorAvatar;
    }

    /**
     * 获取背景图片URL
     *
     * @return 背景图片URL
     */
    public String getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * 获取赞助平台列表
     *
     * @return 赞助平台列表
     */
    public List<JmCreatorSponsor> getSponsors() {
        return sponsors;
    }

    /**
     * 获取关联作品列表
     *
     * @return 关联作品列表
     */
    public List<JmCreatorRelatedWork> getRelatedWorks() {
        return relatedWorks;
    }

    /**
     * 获取筛选条件
     *
     * @return 筛选条件
     */
    public Map<String, Object> getFilters() {
        return filters;
    }
}
