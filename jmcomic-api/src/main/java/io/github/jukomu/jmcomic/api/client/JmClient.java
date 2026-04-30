package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.enums.FavoriteFolderType;
import io.github.jukomu.jmcomic.api.enums.VoteType;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IDownloadPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author JUKOMU
 * @Description: jmcomic-api-java 的核心客户端公开接口，
 * 定义核心业务操作
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public interface JmClient {

    // == 核心数据获取层 ==

    /**
     * 根据本子id获取本子详情
     *
     * @param albumId 本子id
     * @return 本子详情对象
     */
    JmAlbum getAlbum(String albumId);

    /**
     * 获取漫画阅读数据，包含图片列表等信息。
     * 本方法是 {@link #getAlbum(String)} 的补充方法，
     * 即使完全不使用也不影响核心功能。
     *
     * @param comicId 漫画ID
     * @return 阅读数据（包含图片列表的 JmAlbum）
     */
    JmAlbum getComicRead(String comicId);

    /**
     * 根据章节id获取章节详情
     *
     * @param photoId 章节id
     * @return 章节详情对象
     */
    JmPhoto getPhoto(String photoId);

    /**
     * 搜索本子
     *
     * @param query 搜索的参数
     * @return 搜索页的一页结果
     */
    JmSearchPage search(SearchQuery query);

    /**
     * 获取一张图片的二进制数据
     *
     * @param image 图片信息
     * @return 图片的二进制数据
     */
    byte[] fetchImageBytes(JmImage image);

    /**
     * 获取分类排行
     *
     * @param query 分类的参数
     * @return 分类列表页的一页结果
     */
    JmSearchPage getCategories(SearchQuery query);

    /**
     * 获取分类列表（包含分类树和标签块）
     *
     * @return 分类列表
     */
    JmCategoryList getCategoriesList();

    /**
     * 获取本子下载信息
     *
     * @param albumId 本子ID
     * @return 下载信息
     */
    JmAlbumDownloadInfo getAlbumDownloadInfo(String albumId);

    // == 会话/用户管理 ==

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息对象
     */
    JmUserInfo login(String username, String password);

    /**
     * 注册新用户
     *
     * @param username        用户名
     * @param password        密码
     * @param passwordConfirm 确认密码
     * @param email           邮箱
     * @return 注册结果（原始JSON Map）
     */
    @Deprecated
    Map register(String username, String password, String passwordConfirm, String email);

    /**
     * 登出
     */
    void logout();

    /**
     * 忘记密码（发送重置邮件）
     *
     * @param email 邮箱
     */
    @Deprecated
    void forgotPassword(String email);

    /**
     * 获取用户个人资料
     *
     * @param uid 用户ID
     * @return 用户个人资料
     */
    JmUserProfile getUserProfile(String uid);

    /**
     * 编辑用户个人资料
     *
     * @param uid    用户ID
     * @param params 要更新的字段键值对
     * @return 更新后的用户个人资料
     */
    JmUserProfile editUserProfile(String uid, Map<String, String> params);

    // == 评论系统 ==

    /**
     * 获取论坛评论列表（支持本子、小说、博客、用户等多种实体类型）。
     * <p>使用示例：
     * <pre>{@code
     *   // 获取本子评论
     *   client.getComments(ForumQuery.album("1173049").mode("all").page(1));
     *   // 获取小说评论
     *   client.getComments(ForumQuery.novel("12345").mode("manhua").page(1));
     *   // 获取小说某章评论
     *   client.getComments(ForumQuery.novelChapter("12345", "67890").mode("all").page(1));
     * }</pre>
     *
     * @param query 论坛查询参数（见 {@link ForumQuery} 静态工厂方法）
     * @return 评论列表的一页结果
     */
    JmCommentList getComments(ForumQuery query);

    /**
     * 发表本子评论
     *
     * @param entityId    本子ID
     * @param commentText 评论内容
     * @return 用户评论对象
     */
    JmComment postComment(String entityId, String commentText);

    /**
     * 回复本子评论
     *
     * @param entityId        本子ID
     * @param commentText     评论内容
     * @param parentCommentId 被回复评论的id
     * @return 用户评论对象
     */
    JmComment replyToComment(String entityId, String commentText, String parentCommentId);

    /**
     * 对博客发表评论。
     *
     * @param albumId     博客所属本子ID
     * @param blogId      博客ID
     * @param commentText 评论内容
     * @return 用户评论对象
     */
    JmComment postBlogComment(String albumId, String blogId, String commentText);

    /**
     * 回复博客下的评论。
     *
     * @param albumId         博客所属本子ID
     * @param blogId          博客ID
     * @param commentText     评论内容
     * @param parentCommentId 被回复的评论ID
     * @return 用户评论对象
     */
    JmComment replyToBlogComment(String albumId, String blogId, String commentText, String parentCommentId);

    /**
     * 对评论进行投票（点赞/点踩）
     *
     * @param commentId 评论ID
     * @param voteType  投票类型（赞/踩）
     * @return 投票结果
     * @deprecated 该功能已被 JM 平台停用，服务端返回"评价已停用"
     */
    @Deprecated
    JmVoteResult voteComment(String commentId, VoteType voteType);

    // == 收藏系统 ==

    /**
     * 获取收藏夹
     *
     * @param query 收藏夹的参数
     * @return 收藏夹的一页结果
     */
    JmFavoritePage getFavorites(FavoriteQuery query);

    /**
     * 管理收藏夹（添加/重命名/移动/删除文件夹）
     *
     * @param type       操作类型 (add/edit/move/del)
     * @param folderId   文件夹ID
     * @param folderName 文件夹名称（添加/重命名时需要）
     * @param albumId    本子ID（移动时需要）
     * @return 操作结果
     */
    JmFavoriteFolderResult manageFavoriteFolder(FavoriteFolderType type, String folderId, String folderName, String albumId);

    /**
     * 切换本子的收藏状态（收藏/取消收藏）。
     *
     * @param albumId  本子/章节的id
     * @param folderId 收藏夹id（API 客户端当前不传此参数，由 HTML 客户端使用）
     */
    void toggleAlbumFavorite(String albumId, String folderId);

    /**
     * 获取收藏标签列表
     *
     * @return 收藏标签列表
     */
    List<JmTagFavorite> getTagsFavorite();

    /**
     * 添加收藏标签。
     *
     * @param tags 标签名称列表
     */
    void addFavoriteTags(List<String> tags);

    /**
     * 删除收藏标签。
     *
     * @param tags 标签名称列表
     */
    void removeFavoriteTags(List<String> tags);

    // == 点赞 ==

    /**
     * 切换本子点赞状态（点赞/取消点赞）
     *
     * @param albumId 本子ID
     */
    void toggleAlbumLike(String albumId);

    // == 浏览历史 ==

    /**
     * 获取观看历史
     *
     * @param page 页码（从1开始）
     * @return 观看历史列表
     */
    List<JmAlbumMeta> getWatchHistory(int page);

    /**
     * 删除观看历史记录
     *
     * @param id 要删除的漫画ID
     */
    void deleteWatchHistory(String id);

    // == 发现与浏览 ==

    /**
     * 获取热门搜索标签
     *
     * @return 热门标签列表（每个元素为标签文本）
     */
    List<String> getHotTags();

    /**
     * 获取最新上架的本子
     *
     * @param page 页码（从1开始）
     * @return 搜索分页结果
     */
    JmSearchPage getLatest(int page);

    /**
     * 获取随机推荐
     *
     * @return 随机推荐列表
     */
    List<JmAlbumMeta> getRandomRecommend();

    /**
     * 获取首页推广/Banner内容
     *
     * @return 推广数据（原始JSON Map）
     */
    Map getPromote();

    /**
     * 获取连载/系列化列表（分页）
     *
     * @param page 页码（从1开始）
     * @return 连载列表分页结果
     */
    JmSearchPage getSerialization(int page);

    /**
     * 获取每周必看列表
     *
     * @return 每周必看列表数据
     */
    JmWeeklyPicksList getWeeklyPicksList();

    /**
     * 获取每周必看详情
     *
     * @param categoryId 每周必看期数ID，从 getWeeklyPicksList() 获取
     * @return 每周必看详情
     */
    JmWeeklyPicksDetail getWeeklyPicksDetail(String categoryId);

    // == 通知与活动 ==

    /**
     * 获取通知列表
     *
     * @return 通知列表分页结果
     */
    JmNotificationPage getNotifications();

    /**
     * 标记通知
     *
     * @param id   通知ID
     * @param read 0=标记为未读, 1=标记为已读
     */
    void markNotification(String id, int read);

    /**
     * 获取未读通知数量
     *
     * @return 未读通知数量（原始JSON Map）
     */
    Map getUnreadCount();

    /**
     * 获取本子的连载跟踪状态
     *
     * @param id 本子ID
     * @return true=已跟踪, false=未跟踪
     */
    boolean getAlbumSertracking(String id);

    /**
     * 设置本子的连载跟踪状态
     *
     * @param id 本子ID
     */
    void setAlbumSertracking(String id);

    /**
     * 获取连载跟踪列表
     *
     * @param page 页码（从1开始）
     * @return 跟踪列表分页结果
     */
    JmTrackingPage getAlbumTrackingList(int page);

    /**
     * 获取任务列表
     *
     * @param type   任务类型
     * @param filter 筛选条件
     * @return 任务列表
     */
    JmTaskList getTasks(String type, String filter);

    /**
     * 领取任务奖励
     *
     * @param body 任务提交数据键值对
     * @return 领取结果（原始JSON Map）
     */
    @Deprecated
    Map claimTask(Map<String, String> body);

    /**
     * 获取金币购买列表（兑换商店）
     *
     * @param body 请求参数键值对
     * @return 购买列表（原始JSON Map）
     */
    @Deprecated
    Map getCoinBuyList(Map<String, String> body);

    /**
     * 使用金币购买漫画
     *
     * @param comicId 漫画ID
     * @return 购买结果（原始JSON Map）
     */
    @Deprecated
    Map buyComicWithCoin(String comicId);

    /**
     * 充值金币
     *
     * @return 充值结果（原始JSON Map）
     */
    @Deprecated
    Map chargeCoins();

    /**
     * 设置免广告状态
     *
     * @param body 请求参数键值对
     * @return 设置结果（原始JSON Map）
     */
    @Deprecated
    Map setAdFree(Map<String, String> body);

    /**
     * 获取当日签到状态
     *
     * @param userId 用户ID
     * @return 签到状态
     */
    JmDailyCheckInStatus getDailyCheckInStatus(String userId);

    /**
     * 执行每日签到。
     * 签到成功后可通过 {@link #getDailyCheckInStatus(String)} 获取最新签到状态。
     *
     * @param userId  用户ID（当前登录用户的 uid）
     * @param dailyId 签到事件ID（从 {@link #getDailyCheckInStatus(String)} 返回值的 dailyId 字段获取）
     * @throws ResponseException      若已签到过（每日仅可签到一次）
     * @throws ParseResponseException 若响应解析失败
     */
    void doDailyCheckin(String userId, String dailyId);

    /**
     * 获取签到选项列表
     * 返回 List，每个元素为包含 title 等字段的 Map 对象，格式如 {"title":"2024"}
     *
     * @param userId 用户ID
     * @return 签到选项列表（原始JSON List）
     */
    List getDailyCheckInOptions(String userId);

    /**
     * 筛选签到列表
     *
     * @param filter 筛选条件字符串（如年份 "2025"、"2024"，由 getDailyCheckInOptions 返回的 title 值）
     * @return 签到月份列表，每个元素为包含 id/year/month/img 字段的 Map
     */
    List filterDailyCheckInList(String filter);
}
