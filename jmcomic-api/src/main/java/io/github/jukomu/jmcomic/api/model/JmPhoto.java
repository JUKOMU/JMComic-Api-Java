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
