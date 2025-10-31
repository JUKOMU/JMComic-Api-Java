package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 当JMComic服务器返回业务逻辑错误时抛出
 * 例如：登录失败、权限不足、搜索关键词过短等
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public class ApiResponseException extends JmComicException {

    private final int errorCode; // 可选的错误码

    public ApiResponseException(String message) {
        super(message);
        this.errorCode = -1; // -1 表示没有特定的错误码
    }

    public ApiResponseException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 获取业务错误码（如果有）
     *
     * @return 错误码，如果没有则为 -1
     */
    public int getErrorCode() {
        return errorCode;
    }
}
