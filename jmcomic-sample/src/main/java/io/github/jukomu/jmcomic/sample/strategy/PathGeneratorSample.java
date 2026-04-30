package io.github.jukomu.jmcomic.sample.strategy;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IDownloadPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultAlbumPathGenerator;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultPhotoPathGenerator;

import java.nio.file.Path;

/**
 * 演示路径生成策略接口的用法，包括组合、自定义实现和 {@link IDownloadPathGenerator} 总入口。
 *
 * <p>本示例仅展示如何构建路径生成器，不依赖 JmClient 实例。
 */
public class PathGeneratorSample {

    public static void main(String[] args) {
        demoAndThenComposition();
        demoCustomGenerator();
        demoTotalPathGenerator();
    }

    /**
     * 示例1：使用 {@code andThen} 组合多个路径转换。
     * <p>
     * 默认路径为 {@code {author}/{title}/{albumId}}，
     * 通过 andThen 追加自定义子目录。
     */
    private static void demoAndThenComposition() {
        IAlbumPathGenerator baseGenerator = new DefaultAlbumPathGenerator();
        IAlbumPathGenerator withBackup = baseGenerator
                .andThen(path -> path.resolve("_backup"));
        IAlbumPathGenerator withPrefix = withBackup
                .andThen(path -> Path.of("/downloads").resolve(path));

        System.out.println("Album andThen chain configured (3 generators composed).");

        IPhotoPathGenerator photoGenerator = new DefaultPhotoPathGenerator()
                .andThen(path -> path.resolve("thumbnails"));

        System.out.println("Photo andThen chain configured (2 generators composed).");
    }

    /**
     * 示例2：自定义路径生成器实现。
     * <p>
     * 完全控制路径格式，例如按日期归档。
     */
    private static void demoCustomGenerator() {
        String today = java.time.LocalDate.now().toString();

        // 按日期归档的 Album 路径生成器
        IAlbumPathGenerator dateBasedGenerator = album ->
                Path.of("downloads", today, album.getId());

        // 自定义 Photo 路径生成器（仅用序号，不用标题）
        IPhotoPathGenerator indexOnlyGenerator = photo ->
                Path.of(String.format("%03d", photo.getSortOrder()));

        System.out.println("Custom generators created:");
        System.out.println("  Album → downloads/" + today + "/{albumId}");
        System.out.println("  Photo → {sortOrder_3digits}");
    }

    /**
     * 示例3：使用 {@link IDownloadPathGenerator} 总入口。
     * <p>
     * 一次性定义 album/photo/image 三层完整路径，传入 downloadAlbum/downloadPhoto
     * 时将忽略 {@link IAlbumPathGenerator} 和 {@link IPhotoPathGenerator}。
     */
    private static void demoTotalPathGenerator() {
        IDownloadPathGenerator totalGenerator = (JmAlbum a, JmPhoto p, JmImage img) -> {
            String albumDir = a != null ? a.getId() : "unknown_album";
            String photoDir = String.format("%03d", p.getSortOrder());
            String imgName = String.format("%03d_%s", img.getSortOrder(), img.getFilename());
            return Path.of(albumDir, photoDir, imgName);
        };

        System.out.println("IDownloadPathGenerator configured:");
        System.out.println("  Pattern: {albumId}/{photoSortOrder}/{imageSortOrder}_{filename}");

        // 使用方式（需要实际 client 和实体）:
        // client.downloadAlbum(album, totalGenerator, executor);
        // client.downloadPhoto(photo, totalGenerator, executor);  // album 参数传 null
    }
}
