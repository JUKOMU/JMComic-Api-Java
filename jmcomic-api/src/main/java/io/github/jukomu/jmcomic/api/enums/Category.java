package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 定义本子分类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public enum Category {
    ALL("0"),
    DOUJIN("doujin"),
    SINGLE("single"),
    SHORT("short"),
    OTHER("another"),
    KOREAN("hanman"),
    AMERICAN("meiman"),
    COSPLAY("doujin_cosplay"),
    IMAGE_3D("3D");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
