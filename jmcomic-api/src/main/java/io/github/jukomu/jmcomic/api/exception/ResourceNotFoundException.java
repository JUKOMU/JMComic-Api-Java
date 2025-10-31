package io.github.jukomu.jmcomic.api.exception;

/**
 * @author JUKOMU
 * @Description: 表示请求的资源（如本子、章节）未找到的异常的抽象基类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public abstract class ResourceNotFoundException extends JmComicException {

    private final String resourceId;

    public ResourceNotFoundException(String message, String resourceId) {
        super(message);
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message, String resourceId, Throwable cause) {
        super(message, cause);
        this.resourceId = resourceId;
    }

    /**
     * 获取未找到的资源的ID
     *
     * @return 资源ID (例如 "12345")
     */
    public String getResourceId() {
        return resourceId;
    }
}
