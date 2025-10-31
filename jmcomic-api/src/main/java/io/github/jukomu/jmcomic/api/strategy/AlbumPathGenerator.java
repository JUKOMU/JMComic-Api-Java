package io.github.jukomu.jmcomic.api.strategy;

import io.github.jukomu.jmcomic.api.model.JmAlbum;

import java.nio.file.Path;

/**
 * @author JUKOMU
 * @Description: 一个用于生成本子（Album）级别文件或目录路径的策略接口
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
@FunctionalInterface
public interface AlbumPathGenerator {
    /**
     * 根据给定的本子信息，生成一个绝对路径
     *
     * @param album 本子实体
     * @return 资源应保存到的绝对路径
     */
    Path generatePath(JmAlbum album);
}
