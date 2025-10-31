package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 当解析服务器响应（HTML或JSON）失败时抛出
 * 这通常表示JMComic网站前端或API结构发生了变更，导致解析逻辑失效
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class ParseResponseException extends JmComicException {

    public ParseResponseException(String message) {
        super(message);
    }

    public ParseResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
