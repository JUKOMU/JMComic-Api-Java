package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 分类区块（如"主题A漫"、"角色/扮演"等）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCategoryBlock(
        /* 区块标题 */
        String title,
        /* 区块内容标签列表 */
        List<String> content
) {
}
