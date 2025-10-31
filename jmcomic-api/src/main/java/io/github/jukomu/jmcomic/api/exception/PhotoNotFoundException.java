package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 当指定的章节（Photo）不存在时抛出
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class PhotoNotFoundException extends ResourceNotFoundException {

    public PhotoNotFoundException(String photoId) {
        super(String.format("Photo with ID '%s' was not found.", photoId), photoId);
    }

    public PhotoNotFoundException(String photoId, Throwable cause) {
        super(String.format("Photo with ID '%s' was not found.", photoId), photoId, cause);
    }
}