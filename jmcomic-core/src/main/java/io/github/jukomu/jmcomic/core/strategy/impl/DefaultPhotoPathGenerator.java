package io.github.jukomu.jmcomic.core.strategy.impl;

import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;

import java.nio.file.Path;

/**
 * 章节下载路径生成器的默认实现。
 * 生成格式：{sortOrder}_{title}/{photoId}
 * sortOrder 补零到2位以保持字典序，例如 "01_ChapterTitle/123456"。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class DefaultPhotoPathGenerator implements IPhotoPathGenerator {
    @Override
    public Path generatePath(JmPhoto photo) {
        return Path.of(
                FileUtils.sanitizeFilename(String.format("%02d_%s", photo.getSortOrder(), photo.getTitle())),
                photo.getId());
    }
}
