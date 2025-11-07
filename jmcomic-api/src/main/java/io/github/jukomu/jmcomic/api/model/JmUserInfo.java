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
        String avatarUrl,
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

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUid() {
        return uid;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取用户邮箱
     *
     * @return 邮箱地址
     */
    public String getEmail() {
        return email;
    }

    /**
     * 邮箱是否已验证
     *
     * @return true 如果已验证
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * 获取用户头像URL
     *
     * @return 头像URL
     */
    public String getPhotoUrl() {
        return avatarUrl;
    }

    /**
     * 获取用户的名字
     *
     * @return 名字
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * 获取用户性别
     *
     * @return 性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 获取用户信息或消息
     *
     * @return 消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取用户拥有的金币数量
     *
     * @return 金币数量
     */
    public int getCoin() {
        return coin;
    }

    /**
     * 获取用户收藏的本子数量
     *
     * @return 收藏数量
     */
    public int getAlbumFavorites() {
        return albumFavorites;
    }

    /**
     * 获取用户等级
     *
     * @return 等级
     */
    public int getLevel() {
        return level;
    }

    /**
     * 获取用户等级名称
     *
     * @return 等级名称
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * 获取距离下一级所需的经验值
     *
     * @return 下一级经验值
     */
    public long getNextLevelExp() {
        return nextLevelExp;
    }

    /**
     * 获取当前经验值
     *
     * @return 当前经验值
     */
    public long getCurrentExp() {
        return currentExp;
    }

    /**
     * 获取当前等级的经验百分比
     *
     * @return 经验百分比
     */
    public double getExpPercent() {
        return expPercent;
    }

    /**
     * 获取最大收藏本子数量
     *
     * @return 最大收藏数量
     */
    public int getMaxAlbumFavorites() {
        return maxAlbumFavorites;
    }
}
