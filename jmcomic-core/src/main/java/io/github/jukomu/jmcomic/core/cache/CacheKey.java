package io.github.jukomu.jmcomic.core.cache;

import java.util.Objects;

/**
 * @author JUKOMU
 * @Description: 唯一标识一个缓存项
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public final class CacheKey {
    private final Class<?> type;
    private final String id;

    private CacheKey(Class<?> type, String id) {
        this.type = type;
        this.id = id;
    }

    public static CacheKey of(Class<?> type, String id) {
        return new CacheKey(type, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return type.equals(cacheKey.type) && id.equals(cacheKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return "CacheKey{" +
                "type=" + type.getSimpleName() +
                ", id='" + id + '\'' +
                '}';
    }
}
