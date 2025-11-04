package io.github.jukomu.jmcomic.core.cache;

import io.github.jukomu.jmcomic.core.util.JsonUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author JUKOMU
 * @Description: 将对象序列化为JSON，并根据其字节长度来估算对象大小。
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public class CacheObjectSizer {
    public int sizeOf(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return JsonUtils.getGson().toJson(value).getBytes(StandardCharsets.UTF_8).length;
        } catch (Exception e) {
            // 序列化失败时返回一个默认的小尺寸
            return 1;
        }
    }
}
