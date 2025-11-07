package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 封装收藏夹查询的所有参数
 * @Project: jmcomic-api-java
 * @Date: 2025/11/8
 */
public final class FavoriteQuery {
    private final int folderId;
    private final int page;

    private FavoriteQuery(FavoriteQuery.Builder builder) {
        if (builder.folderId < 0) {
            this.folderId = 0;
        } else {
            this.folderId = builder.folderId;
        }
        if (builder.page < 1) {
            this.page = 1;
        } else {
            this.page = builder.page;
        }
    }

    /**
     * 获取收藏夹ID
     * 0表示全部
     *
     * @return 收藏夹ID
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * 获取要获取的页码
     *
     * @return 页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 用于创建 SearchQuery 实例的 Builder
     */
    public static class Builder {
        private int folderId = 0;
        private int page = 1;

        /**
         * 设置收藏夹ID
         * 0表示全部
         *
         * @param folderId 收藏夹ID，例如 0
         */
        public FavoriteQuery.Builder folderId(int folderId) {
            this.folderId = folderId;
            return this;
        }

        /**
         * 设置要获取的页码
         *
         * @param page 页码，必须 >= 1
         */
        public FavoriteQuery.Builder page(int page) {
            this.page = page;
            return this;
        }

        /**
         * 构建一个不可变的 FavoriteQuery 对象
         *
         * @return FavoriteQuery 实例
         */
        public FavoriteQuery build() {
            return new FavoriteQuery(this);
        }
    }
}
