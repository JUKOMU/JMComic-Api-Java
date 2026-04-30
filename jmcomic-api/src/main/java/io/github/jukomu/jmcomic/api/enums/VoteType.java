package io.github.jukomu.jmcomic.api.enums;

/**
 * 评论投票类型
 *
 * @deprecated 该功能已被 JM 平台停用，服务端返回"评价已停用"
 */
@Deprecated
public enum VoteType {
    UP("up", "赞"),
    DOWN("down", "踩");

    private final String value;
    private final String description;

    VoteType(String value, String description) {
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
