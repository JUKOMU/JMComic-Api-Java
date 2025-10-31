package io.github.jukomu.jmcomic.core.net.model;

import io.github.jukomu.jmcomic.api.exception.AlbumNotFoundException;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import okhttp3.Response;

/**
 * @author JUKOMU
 * @Description: 通用禁漫响应类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/31
 */
public class JmResponse extends CommonResponse {

    /**
     * 构造函数
     *
     * @param rawResponse 原始OkHttp响应对象
     */
    public JmResponse(Response rawResponse) {
        super(rawResponse);
    }

    /**
     * 转换构造函数，从一个已有的 CommonResponse 创建
     *
     * @param other 另一个 CommonResponse 实例
     */
    public JmResponse(CommonResponse other) {
        super(other.getRawResponse());
        this.cachedContent = other.getContent(); // 复用已缓存的内容
    }

    /**
     * 判断响应是否成功
     * 除了HTTP状态码为200，还需要响应内容非空
     *
     * @return 如果成功返回 true
     */
    @Override
    public boolean isSuccess() {
        return super.isSuccess() && getContent().length > 0;
    }

    /**
     * 如果请求不成功，则抛出异常
     *
     * @throws ApiResponseException 如果请求不成功
     */
    @Override
    public void requireSuccess() throws ApiResponseException {
        if (isNotSuccess()) {
            throw new ApiResponseException("Request failed with code: " + getHttpCode() + ", error message: " + getErrorMessage());
        }

        // 检查重定向
        if (isRedirect()) {
            // 检查是否重定向为错误页面
            String redirectUrl = getRedirectUrl();
            String originUrl = getOriginUrl();
            if (!redirectUrl.contains("/error/")) {
                return;
            }
            if (redirectUrl.endsWith("/error/album_missing") && !originUrl.endsWith("/error/album_missing")) {
                throw new AlbumNotFoundException("请求的本子不存在");
            } else if (redirectUrl.endsWith("/error/user_missing") && !originUrl.endsWith("/error/user_missing")) {
                throw new ApiResponseException("此用戶名稱不存在，或者你没有登录，請再次確認使用名稱");
            } else if (redirectUrl.endsWith("/error/invalid_module") && !originUrl.endsWith("/error/invalid_module")) {
                throw new ApiResponseException("發生了無法預期的錯誤。若問題持續發生，請聯繫客服支援");
            }
        }
    }

    /**
     * 获取错误消息
     * 子类应覆盖此方法以提供更具体的错误信息
     *
     * @return 错误消息字符串
     */
    public String getErrorMessage() {
        return getText();
    }
}
