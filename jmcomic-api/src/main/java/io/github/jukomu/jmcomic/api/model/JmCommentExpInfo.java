package io.github.jukomu.jmcomic.api.model;

import java.util.List;

/**
 * 评论用户等级信息。
 */
public record JmCommentExpInfo(
        String raw,
        String levelName,
        Integer level,
        Integer nextLevelExp,
        String exp,
        Double expPercent,
        String uid,
        List<String> badges
) {

    public static JmCommentExpInfo empty(String raw) {
        return new JmCommentExpInfo(raw == null ? "" : raw, "", null, null, "", null, "", List.of());
    }
}
