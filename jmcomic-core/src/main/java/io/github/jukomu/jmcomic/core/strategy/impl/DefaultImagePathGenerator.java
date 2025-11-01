package io.github.jukomu.jmcomic.core.strategy.impl;

import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.strategy.IImagePathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;

import java.nio.file.Path;

/**
 * @author JUKOMU
 * @Description: 图片下载路径生成器的默认实现
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class DefaultImagePathGenerator implements IImagePathGenerator {
    @Override
    public Path generatePath(JmImage image) {
        return Path.of(FileUtils.sanitizeFilename(image.getFilename()));
    }
}
