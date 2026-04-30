package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.enums.*;
import io.github.jukomu.jmcomic.api.model.*;

import java.util.Map;

/**
 * @author JUKOMU
 * @Description: jmcomic-api-java 的小说子系统客户端公开接口，
 * @Project: JMComic-Api-Java
 * @Date: 2026/4/30
 */
public interface JmNovelClient {

    // == 小说子系统 ==

    /**
     * 获取小说列表
     *
     * @param order 排序方式（如 "mr" 为最新）
     * @param page  页码（从1开始）
     * @return 小说列表分页结果
     */
    JmNovelPage getNovelList(String order, int page);

    /**
     * 获取小说详情
     *
     * @param novelId 小说ID
     * @return 小说详情
     */
    JmNovelDetail getNovelDetail(String novelId);

    /**
     * 获取小说章节内容
     *
     * @param chapterId 章节ID
     * @param lang      语言代码
     * @return 小说章节内容
     */
    JmNovelChapter getNovelChapter(String chapterId, String lang);

    /**
     * 搜索小说
     *
     * @param searchQuery 搜索关键词
     * @return 小说搜索结果分页
     */
    JmNovelPage searchNovels(String searchQuery);

    /**
     * 切换小说点赞状态（点赞/取消点赞）
     *
     * @param novelId 小说ID
     */
    void toggleNovelLike(String novelId);

    /**
     * 对小说（或小说某章）发表新评论。
     *
     * @param novelId     小说ID
     * @param commentText 评论内容
     * @param chapterId   章节ID，非章节评论时传 null
     * @return 用户评论对象
     */
    JmComment postNovelComment(String novelId, String commentText, String chapterId);

    /**
     * 回复小说（或小说某章）下的某条评论。
     *
     * @param novelId         小说ID
     * @param commentText     评论内容
     * @param parentCommentId 被回复的评论ID
     * @param chapterId       章节ID，非章节评论时传 null
     * @return 用户评论对象
     */
    JmComment replyToNovelComment(String novelId, String commentText, String parentCommentId, String chapterId);

    /**
     * 切换小说收藏状态（收藏/取消收藏）。
     *
     * @param novelId 小说ID
     */
    void toggleNovelFavorite(String novelId);

    /**
     * 获取小说收藏夹列表
     *
     * @param page     页码（从1开始）
     * @param folderId 文件夹ID
     * @param order    排序方式
     * @return 小说收藏夹分页结果
     */
    JmNovelFavoritesPage getNovelFavorites(int page, String folderId, String order);

    /**
     * 管理小说收藏夹（添加/重命名/移动/删除文件夹）
     *
     * @param type       操作类型
     * @param folderId   文件夹ID
     * @param folderName 文件夹名称
     * @param novelId    小说ID（移动时需要）
     * @return 操作结果
     */
    JmFavoriteFolderResult manageNovelFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String novelId);

    /**
     * 购买小说章节（消耗金币）
     *
     * @param chapterId 章节ID
     * @return 购买结果（原始JSON Map）
     */
    @Deprecated
    Map buyNovelChapter(String chapterId);
}
