package io.github.jukomu.jmcomic.api.model;

import java.util.Collections;
import java.util.List;

/**
 * @author JUKOMU
 * @Description: 分类列表项
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCategoryListItem(
        /* 分类ID */
        String id,
        /* 分类名称 */
        String name,
        /* 分类标识 */
        String slug,
        /* 分类类型（可选字段，如 "slug" 或 "search"） */
        String type,
        /* 分类中的专辑总数（响应中可能为数字或字符串） */
        String totalAlbums,
        /* 子分类列表 */
        List<JmSubCategoryItem> subCategories
) {

    public List<JmSubCategoryItem> subCategories() {
        return subCategories != null ? subCategories : Collections.emptyList();
    }
}
