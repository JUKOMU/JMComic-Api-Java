package io.github.jukomu.jmcomic.core.net.model;

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
