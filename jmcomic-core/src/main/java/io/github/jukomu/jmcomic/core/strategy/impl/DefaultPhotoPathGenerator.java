package io.github.jukomu.jmcomic.core.strategy.impl;

import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;

import java.nio.file.Path;

/**
 * @author JUKOMU
 * @Description: 章节下载路径生成器的默认实现
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class DefaultPhotoPathGenerator implements IPhotoPathGenerator {
    @Override
    public Path generatePath(JmPhoto photo) {
        if (photo.isSingleAlbum()) {
            return Path.of("");
        }
        return Path.of(FileUtils.sanitizeFilename(photo.getIndexTitle()),
                photo.getId());
    }
}
