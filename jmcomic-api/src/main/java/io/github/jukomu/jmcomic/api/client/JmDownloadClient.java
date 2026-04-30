package io.github.jukomu.jmcomic.api.client;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IDownloadPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
 * @author JUKOMU
 * @Description: jmcomic-api-java 的下载客户端公开接口，
 * 定义下载相关业务操作，
 * 实现了 AutoCloseable 接口，以便正确管理内部资源（如线程池）
 * @Project: JMComic-Api-Java
 * @Date: 2026/4/30
 */
public interface JmDownloadClient extends AutoCloseable {

    // == 下载操作 ==

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
     * 使用完整路径生成器下载章节，一次性控制 album/photo/image 三层路径。
     * 调用时 album 参数为 null（独立章节下载无本子上下文）。
     *
     * @param photo         章节详情对象
     * @param pathGenerator 完整路径生成器
     * @param executor      线程池
     * @return 下载结果报告
     */
    DownloadResult downloadPhoto(JmPhoto photo, IDownloadPathGenerator pathGenerator, ExecutorService executor);

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

    /**
     * 使用完整路径生成器下载本子，一次性控制 album/photo/image 三层路径。
     *
     * @param album         本子详情对象
     * @param pathGenerator 完整路径生成器
     * @param executor      线程池
     * @return 下载结果报告
     */
    DownloadResult downloadAlbum(JmAlbum album, IDownloadPathGenerator pathGenerator, ExecutorService executor);

    /**
     * 创建本子下载请求，支持链式配置（路径、进度回调等）。
     * <pre>{@code
     * client.download(album)
     *     .withPath(myPath)
     *     .withProgress(p -> updateUI(p))
     *     .execute();
     * }</pre>
     * <p>
     * 默认实现创建不含执行逻辑的请求对象，调用 {@link DownloadRequest#execute()} 将抛出异常。
     * 客户端实现类应覆盖此方法以提供实际下载逻辑。
     *
     * @param album 本子详情对象
     * @return 下载请求构建器
     */
    default DownloadRequest download(JmAlbum album) {
        return new DownloadRequest(album, req -> {
            throw new UnsupportedOperationException(
                    "download(JmAlbum) via DownloadRequest is not implemented by this client. " +
                            "Use a client that overrides this method.");
        });
    }

    /**
     * 创建章节下载请求，支持链式配置（路径、进度回调等）。
     * <pre>{@code
     * client.download(photo)
     *     .withProgress(p -> updateUI(p))
     *     .execute();
     * }</pre>
     * <p>
     * 默认实现创建不含执行逻辑的请求对象，调用 {@link DownloadRequest#execute()} 将抛出异常。
     * 客户端实现类应覆盖此方法以提供实际下载逻辑。
     *
     * @param photo 章节详情对象
     * @return 下载请求构建器
     */
    default DownloadRequest download(JmPhoto photo) {
        return new DownloadRequest(photo, req -> {
            throw new UnsupportedOperationException(
                    "download(JmPhoto) via DownloadRequest is not implemented by this client. " +
                            "Use a client that overrides this method.");
        });
    }

    @Override
    void close();
}
