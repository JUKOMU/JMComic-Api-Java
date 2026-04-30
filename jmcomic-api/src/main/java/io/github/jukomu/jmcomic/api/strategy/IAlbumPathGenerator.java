package io.github.jukomu.jmcomic.api.strategy;

import io.github.jukomu.jmcomic.api.model.JmAlbum;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * 本子（Album）级别文件/目录路径生成策略接口。
 * 支持通过 {@link #andThen(Function)} 进行策略组合。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
@FunctionalInterface
public interface IAlbumPathGenerator {
    /**
     * 根据给定的本子信息，生成一个路径。
     *
     * @param album 本子实体
     * @return 路径
     */
    Path generatePath(JmAlbum album);

    /**
     * 在当前路径生成器之后追加一个转换步骤，用于组合策略。
     *
     * @param after 对生成路径的后置转换
     * @return 组合后的新路径生成器
     */
    default IAlbumPathGenerator andThen(Function<Path, Path> after) {
        return album -> after.apply(generatePath(album));
    }
}
