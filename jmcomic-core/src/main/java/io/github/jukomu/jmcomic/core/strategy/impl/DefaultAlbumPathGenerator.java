package io.github.jukomu.jmcomic.core.strategy.impl;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;

import java.nio.file.Path;

/**
 * 本子下载路径生成器的默认实现。
 * 生成格式：{author}/{title}/{albumId}
 * 作者名缺失时降级为 "unknown_author"。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class DefaultAlbumPathGenerator implements IAlbumPathGenerator {
    @Override
    public Path generatePath(JmAlbum album) {
        String author = album.getPrimaryAuthor();
        if (author == null || author.isBlank()) {
            author = "unknown_author";
        }
        return Path.of(
                FileUtils.sanitizeFilename(author),
                FileUtils.sanitizeFilename(album.getTitle()),
                album.getId());
    }
}
