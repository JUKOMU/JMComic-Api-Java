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

    /**
     * 获取章节ID
     *
     * @return 章节ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取章节标题
     *
     * @return 章节标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取章节在本子中的顺序
     *
     * @return 顺序
     */
    public int getSortOrder() {
        return sortOrder;
    }
}
