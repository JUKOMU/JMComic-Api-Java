package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 定义本子分类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public enum Category {
    ALL("0", "全部"),
    DOUJIN("doujin", "同人"),
    SINGLE("single", "单本"),
    SHORT("short", "短篇"),
    OTHER("another", "其他"),
    KOREAN("hanman", "韩漫"),
    AMERICAN("meiman", "美漫"),
    COSPLAY("doujin_cosplay", "cosplay"),
    IMAGE_3D("3D", "3D");

    private final String value;
    private final String description;

    Category(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
