package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 代表登录成功后返回的用户详细信息
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public record JmUserInfo(
        String uid,
        String username,
        String email,
        boolean emailVerified,
        String photoUrl,
        String firstName,
        String gender,
        String message,
        int coin,
        int albumFavorites,
        int level,
        String levelName,
        long nextLevelExp,
        long currentExp,
        double expPercent,
        int maxAlbumFavorites
) {
    /**
     * 创建一个仅包含用户名的部分填充的 JmUserInfo 对象。
     * 主要用于 JmHtmlClientImpl。
     */
    public static JmUserInfo partial(String username) {
        return new JmUserInfo(
                null, username, null, false, null, null, null, null,
                0, 0, 0, null, 0, 0, 0.0, 0
        );
    }
}
