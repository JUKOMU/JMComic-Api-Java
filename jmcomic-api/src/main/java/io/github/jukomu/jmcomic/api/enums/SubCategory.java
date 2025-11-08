package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 副分类
 * @Project: jmcomic-api-java
 * @Date: 2025/11/9
 */
public enum SubCategory {

    SUB_CHINESE("chinese", "中文"),
    SUB_JAPANESE("japanese", "日语"),
    SUB_ANOTHER_OTHER("other", "其他"),
    SUB_ANOTHER_3D("3d", "3D"),
    SUB_ANOTHER_COSPLAY("cosplay", "cosplay"),
    SUB_DOUJIN_CG("CG", "CG"),
    SUB_SINGLE_YOUTH("youth", "青年漫");

    private final String value;
    private final String description;

    SubCategory(String value, String description) {
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
