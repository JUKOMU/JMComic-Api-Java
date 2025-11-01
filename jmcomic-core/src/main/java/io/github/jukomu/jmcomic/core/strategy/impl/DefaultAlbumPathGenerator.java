package io.github.jukomu.jmcomic.core.strategy.impl;

import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;

import java.nio.file.Path;

/**
 * @author JUKOMU
 * @Description: 本子下载路径生成器的默认实现
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class DefaultAlbumPathGenerator implements IAlbumPathGenerator {
    @Override
    public Path generatePath(JmAlbum album) {
        return Path.of(FileUtils.sanitizeFilename(album.getPrimaryAuthor()),
                FileUtils.sanitizeFilename(album.getTitle()),
                album.getId());
    }
}
