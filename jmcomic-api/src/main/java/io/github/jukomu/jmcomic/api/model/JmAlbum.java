package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * @author JUKOMU
 * @Description: 代表一个本子（Album）的完整详细信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmAlbum(
        /*
          本子ID (例如 "12345")。
         */
        String id,
        /*
          本子标题。
         */
        String title,
        /*
          本子的描述信息。
         */
        String description,
        /*
          用于图片解密的 scramble ID。
         */
        String scrambleId,
        /*
          上架日期字符串。
         */
        String releaseDate,
        /*
          最后更新日期字符串。
         */
        String updateDate,
        /*
          总页数。
         */
        int pageCount,
        /*
          "喜欢"数量的字符串表示 (例如 "1.2K")。
         */
        String likes,
        /*
          "观看"数量的字符串表示 (例如 "40.1K")。
         */
        String views,
        /*
          评论总数。
         */
        int commentCount,
        /*
          作者列表。
         */
        List<String> authors,
        /*
          "作品" 列表 (通常与作者相关)。
         */
        List<String> works,
        /*
          登场角色列表。
         */
        List<String> actors,
        /*
          标签列表。
         */
        List<String> tags,
        /*
          "相关作品"的摘要信息列表。
         */
        List<JmAlbumMeta> relatedAlbums,
        /*
          此本子包含的所有章节的元数据列表。
          要获取章节的完整信息（如图集），需要使用 JmClient.getPhoto()。
         */
        List<JmPhotoMeta> photoMetas
) {
    /**
     * 获取主要作者。通常是作者列表中的第一个。
     *
     * @return 如果作者列表不为空，返回第一个作者；否则返回 "Unknown"。
     */
    public String getPrimaryAuthor() {
        return (authors == null || authors.isEmpty()) ? "Unknown" : authors.get(0);
    }

    /**
     * 判断是否为单行本
     *
     * @return 如果只有一个章节则为单行本
     */
    public boolean isSingleAlbum() {
        return photoMetas.size() == 1;
    }

    /**
     * 根据章节顺序获取章节概要信息 photoIndex>=1
     *
     * @return photoIndex<0 返回第一个章节, 若photoIndex大于总章节数则返回最后一个章节
     */
    public JmPhotoMeta getPhotoMeta(int photoIndex) {
        if (photoIndex < 0) {
            return photoMetas.get(0);
        }
        if (photoIndex >= photoMetas.size()) {
            return photoMetas.get(photoMetas.size() - 1);
        }
        return photoMetas.get(photoIndex - 1);
    }
}
