package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 当指定的本子（Album）不存在时抛出
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class AlbumNotFoundException extends ResourceNotFoundException {

    public AlbumNotFoundException(String albumId) {
        super(String.format("Album with ID '%s' was not found.", albumId), albumId);
    }

    public AlbumNotFoundException(String albumId, Throwable cause) {
        super(String.format("Album with ID '%s' was not found.", albumId), albumId, cause);
    }
}