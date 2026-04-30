package io.github.jukomu.jmcomic.api.model;

import io.github.jukomu.jmcomic.api.enums.ForumMode;

/**
 * @author JUKOMU
 * @Description: 论坛评论列表查询参数，遵循 {@link SearchQuery} 的 Builder 模式。
 * 通过静态工厂方法指定实体类型（album/novel/novelChapter/blog/user）。
 * <p>使用示例：
 * <pre>{@code
 *   // 获取本子评论
 *   client.getComments(ForumQuery.album("1173049").mode("all").page(1));
 *   // 获取小说评论
 *   client.getComments(ForumQuery.novel("12345").mode("manhua").page(1));
 *   // 获取小说某章评论
 *   client.getComments(ForumQuery.novelChapter("12345", "67890").mode("all").page(1));
 *   // 获取博客评论
 *   client.getComments(ForumQuery.blog("blogId").mode("all").page(1));
 * }</pre>
 * @Project: jmcomic-api-java
 * @Date: 2026/4/30
 */
public final class ForumQuery {

    private final String idParam;
    private final String entityId;
    private final String chapterId;
    private final ForumMode mode;
    private final int page;

    private ForumQuery(Builder builder) {
        if (builder.entityId == null || builder.entityId.isEmpty()) {
            throw new IllegalArgumentException("Entity ID cannot be null or empty");
        }
        if (builder.page < 1) {
            throw new IllegalArgumentException("Page number must be >= 1");
        }
        this.idParam = builder.idParam;
        this.entityId = builder.entityId;
        this.chapterId = builder.chapterId;
        this.mode = builder.mode;
        this.page = builder.page;
    }

    /**
     * 创建查询本子评论的 Builder。
     *
     * @param albumId 本子ID
     * @return Builder 实例
     */
    public static Builder album(String albumId) {
        return new Builder().album(albumId);
    }

    /**
     * 创建查询小说评论的 Builder。
     *
     * @param novelId 小说ID
     * @return Builder 实例
     */
    public static Builder novel(String novelId) {
        return new Builder().novel(novelId);
    }

    /**
     * 创建查询小说某章评论的 Builder。
     *
     * @param novelId   小说ID
     * @param chapterId 章节ID
     * @return Builder 实例
     */
    public static Builder novelChapter(String novelId, String chapterId) {
        return new Builder().novel(novelId).chapter(chapterId);
    }

    /**
     * 创建查询博客评论的 Builder。
     *
     * @param blogId 博客ID
     * @return Builder 实例
     */
    public static Builder blog(String blogId) {
        return new Builder().blog(blogId);
    }

    /**
     * 创建查询用户评论的 Builder。
     *
     * @param userId 用户ID
     * @return Builder 实例
     */
    public static Builder user(String userId) {
        return new Builder().user(userId);
    }

    public String getIdParam() {
        return idParam;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public ForumMode getMode() {
        return mode;
    }

    public int getPage() {
        return page;
    }

    /**
     * ForumQuery 的 Builder。
     */
    public static final class Builder {
        private String idParam = "aid";
        private String entityId;
        private String chapterId;
        private ForumMode mode = ForumMode.ALL;
        private int page = 1;

        public Builder album(String albumId) {
            this.idParam = "aid";
            this.entityId = albumId;
            return this;
        }

        public Builder novel(String novelId) {
            this.idParam = "nid";
            this.entityId = novelId;
            return this;
        }

        public Builder blog(String blogId) {
            this.idParam = "bid";
            this.entityId = blogId;
            return this;
        }

        public Builder user(String userId) {
            this.idParam = "uid";
            this.entityId = userId;
            return this;
        }

        public Builder chapter(String chapterId) {
            this.chapterId = chapterId;
            return this;
        }

        public Builder mode(ForumMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public ForumQuery build() {
            return new ForumQuery(this);
        }
    }
}
