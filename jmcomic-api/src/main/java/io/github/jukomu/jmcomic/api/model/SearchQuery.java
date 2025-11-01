package io.github.jukomu.jmcomic.api.model;

import io.github.jukomu.jmcomic.api.enums.Category;
import io.github.jukomu.jmcomic.api.enums.OrderBy;
import io.github.jukomu.jmcomic.api.enums.TimeOption;

import java.util.Objects;
import java.util.Optional;

/**
 * @author JUKOMU
 * @Description: 封装搜索和分类查询的所有参数
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class SearchQuery {

    private final String searchQuery;
    private final OrderBy orderBy;
    private final TimeOption timeOption;
    private final Category category;
    private final String subCategory; // 网页端特有
    private final int page;

    private SearchQuery(Builder builder) {
        this.searchQuery = Objects.requireNonNull(builder.searchQuery, "Search query text cannot be null");
        this.orderBy = Objects.requireNonNull(builder.orderBy, "Order by cannot be null");
        this.timeOption = Objects.requireNonNull(builder.timeOption, "Time option cannot be null");
        this.category = Objects.requireNonNull(builder.category, "Category cannot be null");
        this.subCategory = builder.subCategory;
        if (builder.page < 1) {
            throw new IllegalArgumentException("Page number must be greater than or equal to 1");
        }
        this.page = builder.page;
    }

    /**
     * 获取搜索关键词
     *
     * @return 搜索文本
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * 获取排序方式
     *
     * @return 排序枚举
     */
    public OrderBy getOrderBy() {
        return orderBy;
    }

    /**
     * 获取时间范围
     *
     * @return 时间范围枚举
     */
    public TimeOption getTimeOption() {
        return timeOption;
    }

    /**
     * 获取主分类
     *
     * @return 主分类枚举
     */
    public Category getCategory() {
        return category;
    }

    /**
     * 获取网页端特有的子分类
     *
     * @return 子分类的 Optional 包装
     */
    public Optional<String> getSubCategory() {
        return Optional.ofNullable(subCategory);
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
        private String searchQuery = "";
        private OrderBy orderBy = OrderBy.LATEST;
        private TimeOption timeOption = TimeOption.ALL;
        private Category category = Category.ALL;
        private String subCategory = null;
        private int page = 1;

        /**
         * 设置搜索关键词
         *
         * @param searchQuery 搜索文本，例如 "無修正"
         */
        public Builder text(String searchQuery) {
            this.searchQuery = searchQuery;
            return this;
        }

        /**
         * 设置排序方式
         *
         * @param orderBy 排序枚举
         */
        public Builder orderBy(OrderBy orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        /**
         * 设置时间范围
         *
         * @param timeOption 时间范围枚举
         */
        public Builder time(TimeOption timeOption) {
            this.timeOption = timeOption;
            return this;
        }

        /**
         * 设置主分类
         *
         * @param category 主分类枚举
         */
        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        /**
         * (高级) 设置网页端特有的子分类
         *
         * @param subCategory 子分类的字符串值，例如 "chinese"
         */
        public Builder subCategory(String subCategory) {
            this.subCategory = subCategory;
            return this;
        }

        /**
         * 设置要获取的页码
         *
         * @param page 页码，必须 >= 1
         */
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /**
         * 构建一个不可变的 SearchQuery 对象
         *
         * @return SearchQuery 实例
         */
        public SearchQuery build() {
            return new SearchQuery(this);
        }
    }
}
