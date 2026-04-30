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
          添加时间（Unix 时间戳字符串），取自 API addtime 字段。
         */
        String addTime,
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
          封面图片URL。
         */
        String image,
        /*
          主分类信息。
         */
        JmCategoryMeta category,
        /*
          子分类信息。
         */
        JmCategoryMeta subCategory,
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
        List<JmPhotoMeta> photoMetas,
        /*
          系列ID。"0" 表示单行本，非 "0" 表示多章节本子。
         */
        String seriesId,
        /*
          当前用户是否已收藏该本子。
         */
        boolean isFavorite,
        /*
          当前用户是否已点赞该本子。
         */
        boolean liked,
        /*
          是否为成人内容标识。
         */
        boolean isAids,
        /*
          本子包含的图片列表，由 getComicRead 填充。
         */
        List<JmImage> images,
        /*
          价格，通常为空字符串，部分本子可能有数据。
         */
        String price,
        /*
          是否已购买，通常为空字符串，部分本子可能有数据。
         */
        String purchased
) {
    /**
     * 获取本子ID。
     *
     * @return 本子ID (例如 "12345")。
     */
    public String getId() {
        return id;
    }

    /**
     * 获取本子标题。
     *
     * @return 本子标题。
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取本子的描述信息。
     *
     * @return 本子的描述信息。
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取用于图片解密的 scramble ID。
     *
     * @return 用于图片解密的 scramble ID。
     */
    public String getScrambleId() {
        return scrambleId;
    }

    /**
     * 获取添加时间（Unix 时间戳字符串）。
     *
     * @return 添加时间（Unix 时间戳字符串）。
     */
    public String getAddTime() {
        return addTime;
    }

    /**
     * 获取总页数。
     *
     * @return 总页数。
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * 获取"喜欢"数量的字符串表示。
     *
     * @return "喜欢"数量的字符串表示 (例如 "1.2K")。
     */
    public String getLikes() {
        return likes;
    }

    /**
     * 获取"观看"数量的字符串表示。
     *
     * @return "观看"数量的字符串表示 (例如 "40.1K")。
     */
    public String getViews() {
        return views;
    }

    /**
     * 获取评论总数。
     *
     * @return 评论总数。
     */
    public int getCommentCount() {
        return commentCount;
    }

    /**
     * 获取封面图片URL。
     *
     * @return 封面图片URL。
     */
    public String getImage() {
        return image;
    }

    /**
     * 获取主分类信息。
     *
     * @return 主分类信息。
     */
    public JmCategoryMeta getCategory() {
        return category;
    }

    /**
     * 获取子分类信息。
     *
     * @return 子分类信息。
     */
    public JmCategoryMeta getSubCategory() {
        return subCategory;
    }

    /**
     * 获取作者列表。
     *
     * @return 作者列表。
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * 获取"作品" 列表。
     *
     * @return "作品" 列表 (通常与作者相关)。
     */
    public List<String> getWorks() {
        return works;
    }

    /**
     * 获取登场角色列表。
     *
     * @return 登场角色列表。
     */
    public List<String> getActors() {
        return actors;
    }

    /**
     * 获取标签列表。
     *
     * @return 标签列表。
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 获取"相关作品"的摘要信息列表。
     *
     * @return "相关作品"的摘要信息列表。
     */
    public List<JmAlbumMeta> getRelatedAlbums() {
        return relatedAlbums;
    }

    /**
     * 获取此本子包含的所有章节的元数据列表。
     *
     * @return 要获取章节的完整信息（如图集），需要使用 JmClient.getPhoto()。
     */
    public List<JmPhotoMeta> getPhotoMetas() {
        return photoMetas;
    }

    /**
     * 获取系列ID。
     *
     * @return 系列ID，"0" 表示单行本。
     */
    public String getSeriesId() {
        return seriesId;
    }

    /**
     * 获取当前用户是否已收藏。
     *
     * @return true 表示已收藏。
     */
    public boolean isFavorite() {
        return isFavorite;
    }

    /**
     * 获取当前用户是否已点赞。
     *
     * @return true 表示已点赞。
     */
    public boolean isLiked() {
        return liked;
    }

    /**
     * 获取是否为成人内容标识。
     *
     * @return true 表示为成人内容。
     */
    public boolean isAids() {
        return isAids;
    }

    /**
     * 获取本子包含的图片列表。
     *
     * @return 图片列表，由 getComicRead 填充。
     */
    public List<JmImage> getImages() {
        return images;
    }

    /**
     * 获取价格。
     *
     * @return 价格字符串，通常为空。
     */
    public String getPrice() {
        return price;
    }

    /**
     * 获取是否已购买。
     *
     * @return 是否已购买，通常为空字符串。
     */
    public String getPurchased() {
        return purchased;
    }

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
        return photoMetas.size() <= 1;
    }

    /**
     * 根据章节顺序获取章节概要信息 photoIndex>=1
     *
     * @return {@code photoIndex <= 0} 返回第一个章节，若 {@code photoIndex} 大于总章节数则返回最后一个章节
     */
    public JmPhotoMeta getPhotoMeta(int photoIndex) {
        if (photoIndex <= 0) {
            return photoMetas.get(0);
        }
        if (photoIndex >= photoMetas.size()) {
            return photoMetas.get(photoMetas.size() - 1);
        }
        return photoMetas.get(photoIndex - 1);
    }
}
