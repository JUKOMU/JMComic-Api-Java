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
        List<String> tags,
        /*
          描述
         */
        String description,
        /*
          封面图
         */
        String image,
        /*
          主分类
         */
        JmCategoryMeta category,
        /*
          子分类
         */
        JmCategoryMeta subCategory
) {

    public JmAlbumMeta(String id, String title, List<String> authors, List<String> tags) {
        this(id, title, authors, tags, null, null, null, null);
    }

    /**
     * 获取本子ID
     *
     * @return 本子ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取本子标题
     *
     * @return 本子标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取作者列表
     *
     * @return 作者列表
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取封面图
     *
     * @return 封面图
     */
    public String getImage() {
        return image;
    }

    /**
     * 获取主分类
     *
     * @return 主分类
     */
    public JmCategoryMeta getCategory() {
        return category;
    }

    /**
     * 获取子分类
     *
     * @return 子分类
     */
    public JmCategoryMeta getSubCategory() {
        return subCategory;
    }
}
