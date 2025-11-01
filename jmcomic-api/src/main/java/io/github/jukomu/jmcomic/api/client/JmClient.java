package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IImagePathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import okhttp3.Cookie;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author JUKOMU
 * @Description: jmcomic-api-java 的核心客户端公开接口，
 * 定义了所有与JMComic交互的业务操作，
 * 实现了 AutoCloseable 接口，以便正确管理内部资源（如线程池）
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public interface JmClient extends AutoCloseable {

    // == 核心数据获取层 ==

    /**
     * 根据本子id获取本子详情
     *
     * @param albumId 本子id
     * @return 本子详情对象
     */
    JmAlbum getAlbum(String albumId);

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
     * 获取收藏夹
     *
     * @param page 页码
     * @return 收藏夹的一页结果
     */
    JmFavoritePage getFavorites(int page);

    /**
     * 获取分类排行
     *
     * @param query 分类的参数
     * @return 分类列表页的一页结果
     */
    JmSearchPage getCategories(SearchQuery query);

    /**
     * 获取一张图片的二进制数据
     *
     * @param image 图片信息
     * @return 图片的二进制数据
     */
    byte[] fetchImageBytes(JmImage image);

    // == 会话管理层 ==

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息对象
     */
    JmUserInfo login(String username, String password);

    List<Cookie> getCookies();

    void setCookies(List<Cookie> cookies);

    // == 用户交互层 ==

    /**
     * 发表评论
     *
     * @param entityId    本子/章节的id
     * @param commentText 评论内容
     * @param status      是否有剧透
     * @return 用户评论对象
     */
    JmComment postComment(String entityId, String commentText, String status);

    /**
     * 回复评论
     *
     * @param entityId        本子/章节的id
     * @param commentText     评论内容
     * @param parentCommentId 被回复评论的id
     * @return 用户评论对象
     */
    JmComment replyToComment(String entityId, String commentText, String parentCommentId);

    /**
     * 添加本子到收藏夹
     *
     * @param albumId  本子/章节的id
     * @param folderId 收藏夹id
     */
    void addAlbumToFavorite(String albumId, String folderId);

    // == 便利操作层 ==

    /**
     * 下载图片到默认路径
     *
     * @param image 图片信息
     */
    void downloadImage(JmImage image) throws IOException;

    /**
     * 下载图片
     *
     * @param imageUrl 图片URL
     */
    void downloadImage(String imageUrl, Path path) throws IOException;

    /**
     * 下载图片到指定路径
     *
     * @param image              图片信息
     * @param imagePathGenerator 路径
     */
    void downloadImage(JmImage image, IImagePathGenerator imagePathGenerator) throws IOException;

    /**
     * 下载图片到指定路径
     *
     * @param image 图片信息
     * @param path  路径
     */
    void downloadImage(JmImage image, Path path) throws IOException;

    /**
     * 下载章节到默认路径
     *
     * @param photo 章节详情对象
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo);

    /**
     * 下载章节到指定路径
     *
     * @param photo         章节详情对象
     * @param pathGenerator 路径
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo, IPhotoPathGenerator pathGenerator);

    /**
     * 下载章节到指定路径
     *
     * @param photo 章节详情对象
     * @param path  路径
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo, Path path);

    /**
     * 下载章节到指定路径
     *
     * @param photo         章节详情对象
     * @param pathGenerator 路径
     * @param executor      线程池
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo, IPhotoPathGenerator pathGenerator, ExecutorService executor);

    /**
     * 下载章节到指定路径
     *
     * @param photo    章节详情对象
     * @param path     路径
     * @param executor 线程池
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo, Path path, ExecutorService executor);

    /**
     * 下载本子到指定路径
     *
     * @param album 本子详情对象
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album);

    /**
     * 下载本子到指定路径
     *
     * @param album         本子详情对象
     * @param pathGenerator 路径
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album, IAlbumPathGenerator pathGenerator);

    /**
     * 下载本子到指定路径
     *
     * @param album 本子详情对象
     * @param path  路径
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album, Path path);

    /**
     * 下载本子到指定路径
     *
     * @param album         本子详情对象
     * @param pathGenerator 路径
     * @param executor      线程池
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album, IAlbumPathGenerator pathGenerator, ExecutorService executor);

    /**
     * 下载本子到指定路径
     *
     * @param album    本子详情对象
     * @param path     路径
     * @param executor 线程池
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album, Path path, ExecutorService executor);

    // == 资源管理 ==

    @Override
    void close();
}
