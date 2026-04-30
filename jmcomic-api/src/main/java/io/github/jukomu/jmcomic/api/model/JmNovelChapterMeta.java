package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 小说详情中的章节列表项（series 字段）
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmNovelChapterMeta(
        /* 小说关联的章节ID（NCID） */
        String NCID,
        /* 章节标题 */
        String title,
        /* 排序序号 */
        String sort,
        /* 创建时间 */
        String createdAt,
        /* 状态 */
        String status,
        /* 发布时间 */
        String onAt,
        /* 购买所需积分 */
        String buyNc,
        /* 是否需要购买 */
        String isNeedBuyNc,
        /* 是否需要登录 */
        String isNeedLogin,
        /* 章节ID */
        String id,
        /* 章节名称 */
        String name,
        /* 是否为新章节 */
        boolean isNew,
        /* 是否已购买 */
        boolean purchased
) {

    /**
     * 获取小说关联的章节ID（NCID）
     *
     * @return NCID
     */
    public String getNCID() {
        return NCID;
    }

    /**
     * 获取章节标题
     *
     * @return 章节标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取排序序号
     *
     * @return 排序序号
     */
    public String getSort() {
        return sort;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取发布时间
     *
     * @return 发布时间
     */
    public String getOnAt() {
        return onAt;
    }

    /**
     * 获取购买所需积分
     *
     * @return 购买所需积分
     */
    public String getBuyNc() {
        return buyNc;
    }

    /**
     * 获取是否需要购买
     *
     * @return 是否需要购买
     */
    public String getIsNeedBuyNc() {
        return isNeedBuyNc;
    }

    /**
     * 获取是否需要登录
     *
     * @return 是否需要登录
     */
    public String getIsNeedLogin() {
        return isNeedLogin;
    }

    /**
     * 获取章节ID
     *
     * @return 章节ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取章节名称
     *
     * @return 章节名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取是否为新章节
     *
     * @return 是否为新章节
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * 获取是否已购买
     *
     * @return 是否已购买
     */
    public boolean isPurchased() {
        return purchased;
    }
}
