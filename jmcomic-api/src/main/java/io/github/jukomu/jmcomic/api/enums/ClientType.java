package io.github.jukomu.jmcomic.api.enums;

/**
 * @author JUKOMU
 * @Description: 定义了JmClient的实现类型
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public enum ClientType {
    /**
     * 基于JMComic移动端API的客户端，通常更稳定、高效
     */
    API,

    /**
     * 基于抓取和解析JMComic网页HTML的客户端
     */
    HTML
}
