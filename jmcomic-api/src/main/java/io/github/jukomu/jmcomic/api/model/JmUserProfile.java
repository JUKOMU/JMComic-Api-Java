package io.github.jukomu.jmcomic.api.model;

/**
 * @author JUKOMU
 * @Description: 用户个人资料模型
 *               包含昵称、生日、地区、职业、社交信息等可编辑字段
 * @Project: jmcomic-api-java
 * @Date: 2026/04/29
 */
public record JmUserProfile(
        /* 用户名（只读） */
        String username,
        /* 邮箱（只读） */
        String email,
        /* 昵称 */
        String nickname,
        /* 姓 */
        String lastName,
        /* 名 */
        String firstName,
        /* 生日 */
        String birthday,
        /* 感情状态 (Single / Taken / Open) */
        String relations,
        /* 性取向 (Guys / Girls / Guys + Girls) */
        String sexuality,
        /* 个人网站 */
        String website,
        /* 出生地 */
        String birthPlace,
        /* 城市 */
        String city,
        /* 国家 */
        String country,
        /* 职业 */
        String occupation,
        /* 公司 */
        String company,
        /* 学校 */
        String school,
        /* 关于我 */
        String aboutMe,
        /* 其他信息 */
        String infoHere,
        /* 收藏类别 */
        String collections,
        /* 理想型 */
        String ideal,
        /* Erogenic区域 */
        String erogenic,
        /* 喜好 */
        String favorite,
        /* 讨厌 */
        String hate
) {
}
