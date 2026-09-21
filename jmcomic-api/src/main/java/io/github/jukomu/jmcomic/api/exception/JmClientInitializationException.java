package io.github.jukomu.jmcomic.api.exception;

/**
 * 客户端初始化失败时抛出的异常。
 *
 * @author JUKOMU
 * @Description: 封装客户端初始化阶段发生的错误，并保留原始异常原因
 * @Project: jmcomic-api-java
 * @Date: 2026/9/21
 */
public class JmClientInitializationException extends JmComicException {

    public JmClientInitializationException(String message) {
        super(message);
    }

    public JmClientInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
