package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 代表一个章节（Photo）的轻量级元数据或摘要信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmPhotoMeta(
        /*
          章节ID
         */
        String id,
        /*
          章节标题
         */
        String title,
        /*
          章节在本子中的顺序，从1开始
         */
        int sortOrder
) {
}
