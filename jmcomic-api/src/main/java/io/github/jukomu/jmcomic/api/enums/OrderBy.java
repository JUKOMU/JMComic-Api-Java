package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 定义搜索和分类时的排序选项
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public enum OrderBy {
    /**
     * 最新
     */
    LATEST("mr"),
    /**
     * 最多观看
     */
    MOST_VIEWED("mv"),
    /**
     * 图片最多
     */
    MOST_IMAGES("mp"),
    /**
     * 最多喜欢
     */
    MOST_LIKED("tf");

    private final String value;

    OrderBy(String value) {
        this.value = value;
    }

    /**
     * 获取在API请求中使用的实际字符串值
     *
     * @return API参数值
     */
    public String getValue() {
        return value;
    }
}
