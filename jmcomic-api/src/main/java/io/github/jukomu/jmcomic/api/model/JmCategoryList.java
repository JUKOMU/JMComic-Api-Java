package io.github.jukomu.jmcomic.api.model;

import java.util.Collections;
import java.util.List;

/**
 * @author JUKOMU
 * @Description: 分类列表（包含分类和分类区块）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCategoryList(
        /* 分类列表 */
        List<JmCategoryListItem> categories,
        /* 分类区块列表（如"主题A漫"、"角色/扮演"等） */
        List<JmCategoryBlock> blocks
) {

    public List<JmCategoryListItem> categories() {
        return categories != null ? categories : Collections.emptyList();
    }

    public List<JmCategoryBlock> blocks() {
        return blocks != null ? blocks : Collections.emptyList();
    }
}
