package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表一个章节（Photo）的完整详细信息，包含其下的所有图片
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmPhoto(
        /*
          章节ID
         */
        String id,
        /*
          章节标题
         */
        String title,
        /*
          所属本子(Album)的ID
         */
        String albumId,
        /*
          用于图片解密的 scramble ID
         */
        String scrambleId,
        /*
          章节在本子中的顺序，从1开始
         */
        int sortOrder,
        /*
          章节的作者
         */
        String author,
        /*
          章节的标签列表
         */
        List<String> tags,
        /*
          此章节包含的所有图片的列表
         */
        List<JmImage> images
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
     * 获取所属本子(Album)的ID
     *
     * @return 本子ID
     */
    public String getAlbumId() {
        return albumId;
    }

    /**
     * 获取用于图片解密的 scramble ID
     *
     * @return scramble ID
     */
    public String getScrambleId() {
        return scrambleId;
    }

    /**
     * 获取章节在本子中的顺序
     *
     * @return 顺序
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * 获取章节的作者
     *
     * @return 作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 获取章节的标签列表
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 获取此章节包含的所有图片的列表
     *
     * @return 图片列表
     */
    public List<JmImage> getImages() {
        return images;
    }

    /**
     * 判断这是否是一个单章本子（即章节本身就是一个独立的本子）
     *
     * @return 如果是单章本子则为true
     */
    public boolean isSingleAlbum() {
        return id.equals(albumId);
    }

    /**
     * 获取带序号的标题，例如 "第1話 标题"
     *
     * @return 格式化的标题字符串
     */
    public String getIndexTitle() {
        return String.format("第%d話 %s", sortOrder, title);
    }
}
