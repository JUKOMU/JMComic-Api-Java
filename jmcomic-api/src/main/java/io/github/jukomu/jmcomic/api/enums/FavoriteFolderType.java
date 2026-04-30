package io.github.jukomu.jmcomic.api.enums;

/**
 * 收藏夹管理操作类型
 */
public enum FavoriteFolderType {
    ADD("add", "新建文件夹"),
    EDIT("edit", "重命名文件夹"),
    MOVE("move", "移动作品到文件夹"),
    DELETE("del", "删除文件夹");

    private final String value;
    private final String description;

    FavoriteFolderType(String value, String description) {
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
