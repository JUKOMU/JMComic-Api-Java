package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 子分类
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmSubCategoryItem(
        /* 子分类ID */
        String cid,
        /* 子分类名称 */
        String name,
        /* 子分类标识 */
        String slug
) {
}
