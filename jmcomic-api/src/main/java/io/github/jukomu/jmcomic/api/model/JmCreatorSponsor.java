package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 创作者赞助平台信息
 * @Project: jmcomic-api-java
 * @Date: 2026/4/27
 */
public record JmCreatorSponsor(
        /* 赞助平台URL */
        String platformUrl,
        /* 赞助平台名称 */
        String platformName
) {

    /**
     * 获取赞助平台URL
     *
     * @return 赞助平台URL
     */
    public String getPlatformUrl() {
        return platformUrl;
    }

    /**
     * 获取赞助平台名称
     *
     * @return 赞助平台名称
     */
    public String getPlatformName() {
        return platformName;
    }
}
