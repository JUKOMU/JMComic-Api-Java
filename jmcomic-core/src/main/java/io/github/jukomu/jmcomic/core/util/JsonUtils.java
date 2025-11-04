package io.github.jukomu.jmcomic.core.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author JUKOMU
 * @Description: JSON工具类
 * @Project: jmcomic-api-java
 * @Date: 2025/11/5
 */
public final class JsonUtils {
    private static final Gson GSON_INSTANCE = new Gson();

    private JsonUtils() {
    }

    public static Gson getGson() {
        return GSON_INSTANCE;
    }

    public static String toJson(Object object) {
        return GSON_INSTANCE.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON_INSTANCE.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON_INSTANCE.fromJson(json, type);
    }
}
