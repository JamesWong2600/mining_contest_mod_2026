package org.link_uuid.miningcontest.data.cache;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cache<K, V> {
    private static final Map<UUID, CacheEntry<Double>> cache = new HashMap<>();

    private static class CacheEntry<V> {
        private final V value;
        private final long expireTime;

        public CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }

    public static void put(UUID playerId, double distance) {
        cache.put(playerId, new CacheEntry<>(distance, Long.MAX_VALUE));
    }

    // 新增：設置帶有過期時間的值
    public static void setValue(UUID playerId, double distance, long ttlSeconds) {
        long expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(playerId, new CacheEntry<>(distance, expireTime));
    }

    public static double get(UUID playerId) {
        CacheEntry<Double> entry = cache.get(playerId);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cache.remove(playerId);
            }
            return -1.0;
        }
        return entry.getValue();
    }

    public static boolean contains(UUID playerId) {
        CacheEntry<Double> entry = cache.get(playerId);
        if (entry != null && entry.isExpired()) {
            cache.remove(playerId);
            return false;
        }
        return entry != null;
    }

    public static void remove(UUID playerId) {
        cache.remove(playerId);
    }

    // 新增：獲取過期時間（返回剩餘毫秒數）
    public static long getExpireTime(UUID playerId) {
        CacheEntry<Double> entry = cache.get(playerId);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cache.remove(playerId);
            }
            return -1;
        }
        return entry.getExpireTime() - System.currentTimeMillis();
    }
}