package io.github.jukomu.jmcomic.core.net.model;

import io.github.jukomu.jmcomic.api.exception.ResponseException;
import io.github.jukomu.jmcomic.api.exception.ResourceNotFoundException;
import okhttp3.Response;

/**
 * @author JUKOMU
 * @Description: 禁漫网页的HTML响应
 * @Project: jmcomic-api-java
 * @Date: 2025/10/31
 */
public class JmHtmlResponse extends JmResponse {
    /**
     * 构造函数
     *
     * @param rawResponse 原始OkHttp响应对象
     */
    public JmHtmlResponse(Response rawResponse) {
        super(rawResponse);
    }

    public JmHtmlResponse(CommonResponse other) {
        super(other);
    }

    @Override
    public void requireSuccess() throws ResponseException {
        super.requireSuccess();
        // 检查重定向
        if (isRedirect()) {
            // 检查是否重定向为错误页面
            String redirectUrl = getRedirectUrl();
            String originUrl = getOriginUrl();
            if (!redirectUrl.contains("/error/")) {
                return;
            }
            if (redirectUrl.endsWith("/error/album_missing") && !originUrl.endsWith("/error/album_missing")) {
                throw new ResourceNotFoundException("请求的资源不存在", originUrl);
            } else if (redirectUrl.endsWith("/error/user_missing") && !originUrl.endsWith("/error/user_missing")) {
                throw new ResponseException("此用户名称不存在，或者你没有登录，请再次确认使用名称");
            } else if (redirectUrl.endsWith("/error/invalid_module") && !originUrl.endsWith("/error/invalid_module")) {
                throw new ResponseException("发生了无法预期的错误。若问题持续发生，请联系客服支持");
            }
        }
    }

    /**
     * 获取响应的html文本
     * 去除开头非ASCII字符
     *
     * @return 响应的html文本
     */
    public String getHtml() {
        String text = getText();
        if (text == null) {
            return "";
        }

        while (text.charAt(0) < 0 || text.charAt(0) > 127) {
            text = text.substring(1);
        }
        return text;
    }
}
