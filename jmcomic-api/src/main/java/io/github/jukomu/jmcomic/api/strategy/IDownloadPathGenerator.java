package io.github.jukomu.jmcomic.api.strategy;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.model.JmPhoto;

import java.nio.file.Path;

/**
 * 总入口路径生成器 — 一次性定义 album、photo、image 三层的完整下载路径。
 * 当传入此接口时，{@link IAlbumPathGenerator} 和 {@link IPhotoPathGenerator} 将被忽略。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
@FunctionalInterface
public interface IDownloadPathGenerator {
    /**
     * 根据给定的本子、章节、图片信息，生成完整的文件保存路径。
     *
     * @param album 本子实体（独立章节下载时可能为 null）
     * @param photo 章节实体
     * @param image 图片实体
     * @return 图片应保存到的完整路径
     */
    Path generatePath(JmAlbum album, JmPhoto photo, JmImage image);
}
