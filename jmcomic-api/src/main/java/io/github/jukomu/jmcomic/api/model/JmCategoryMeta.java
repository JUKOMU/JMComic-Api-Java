package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 分类摘要
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCategoryMeta(
        /* 分类ID */
        String id,
        /* 分类标题 */
        String title
) {

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取分类标题
     *
     * @return 分类标题
     */
    public String getTitle() {
        return title;
    }
}
