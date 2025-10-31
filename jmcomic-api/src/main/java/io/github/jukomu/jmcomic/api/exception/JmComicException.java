package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 异常基类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class JmComicException extends RuntimeException {

    public JmComicException(String message) {
        super(message);
    }

    public JmComicException(String message, Throwable cause) {
        super(message, cause);
    }
}
