package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表搜索或分类列表页的一页结果
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmSearchPage(
        /*
          当前页码
         */
        int currentPage,
        /*
          结果总数
         */
        int totalItems,
        /*
          总共的页数
         */
        int totalPages,
        /*
          当前页包含的本子摘要信息列表
         */
        List<JmAlbumMeta> content
) {
}
