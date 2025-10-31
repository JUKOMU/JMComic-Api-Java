package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表一个本子（Album）的轻量级元数据或摘要信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmAlbumMeta(
        /*
          本子ID
         */
        String id,
        /*
          本子标题
         */
        String title,
        /*
          作者列表
         */
        List<String> authors,
        /*
          标签列表
         */
        List<String> tags
) {
}
