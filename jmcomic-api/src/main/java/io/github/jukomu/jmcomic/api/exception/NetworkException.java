package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 封装所有与网络通信相关的底层问题，例如多次重试后仍然失败
 * 通常包装了一个底层的 IOException
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class NetworkException extends JmComicException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
