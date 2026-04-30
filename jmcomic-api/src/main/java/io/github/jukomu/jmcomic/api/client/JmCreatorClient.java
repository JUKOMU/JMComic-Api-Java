package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.model.*;

/**
 * @author JUKOMU
 * @Description: jmcomic-api-java 的创作者子系统客户端公开接口，
 * @Project: JMComic-Api-Java
 * @Date: 2026/4/30
 */
public interface JmCreatorClient {

    // == 创作者子系统 ==

    /**
     * 获取创作者/作者列表
     *
     * @param page        页码（从1开始）
     * @param searchQuery 搜索关键词
     * @return 创作者列表分页结果
     */
    JmCreatorPage getCreatorAuthors(int page, String searchQuery);

    /**
     * 获取创作者作品列表
     *
     * @param page        页码（从1开始）
     * @param searchValue 搜索值或分类参数
     * @param lang        语言过滤
     * @param source      来源平台过滤
     * @return 作品列表分页结果
     */
    JmCreatorWorkPage getCreatorWorks(int page, String searchValue, String lang, String source);

    /**
     * 获取指定作者的作品列表
     *
     * @param creatorId 作者ID（发送时使用参数名"id"）
     * @param language  语言过滤（发送时使用参数名"lang"）
     * @param source    来源平台过滤
     * @param page      页码
     * @return 作者作品列表，包含作者信息、赞助平台、关联作品和筛选条件
     */
    JmCreatorAuthorWorksPage getCreatorAuthorWorks(String creatorId, String language, String source, int page);

    /**
     * 获取创作者作品信息
     *
     * @param workId 作品ID
     * @return 创作者作品信息
     */
    JmCreatorWorkInfo getCreatorWorkInfo(String workId);

    /**
     * 获取创作者作品详情
     *
     * @param workId 作品ID
     * @return 作品详情
     */
    JmCreatorWorkDetail getCreatorWorkDetail(String workId);
}
