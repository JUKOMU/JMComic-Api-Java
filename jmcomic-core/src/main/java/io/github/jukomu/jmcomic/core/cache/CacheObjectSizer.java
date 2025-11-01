package io.github.jukomu.jmcomic.core.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;

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
            return JSON.toJSONString(value).getBytes(StandardCharsets.UTF_8).length;
        } catch (JSONException e) {
            // 序列化失败时返回一个默认的小尺寸
            return 1;
        }
    }
}
