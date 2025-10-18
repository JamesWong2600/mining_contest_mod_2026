package org.link_uuid.miningcontest.data.cache;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cache<K, V> {
    private static final Map<UUID, CacheEntry<Double>> cache = new HashMap<>();
    private static final Map<String, CacheEntry<String>> cooldown_cache = new HashMap<>();
    private static final Map<String, CacheEntry<Integer>> server_task_cache = new HashMap<>();
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

    public static void put_server(String task_variable, int distance) { // 改為 int
        server_task_cache.put(task_variable, new CacheEntry<>(distance, Long.MAX_VALUE));
    }

    // 同時添加對應的 get 方法
    public static int get_server(String task_variable) {
        CacheEntry<Integer> entry = server_task_cache.get(task_variable);
        if (entry == null) {
            return -1; // 或者你想要的默認值
        }
        return entry.getValue();
    }

    public static void put(UUID playerId, double distance) {
        cache.put(playerId, new CacheEntry<>(distance, Long.MAX_VALUE));
    }

    // 新增：設置帶有過期時間的值
    public static void setValue(UUID playerId, double distance, long ttlSeconds) {
        long expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(playerId, new CacheEntry<>(distance, expireTime));
    }

    public static void setCooldown(UUID playerId, String value, long ttlSeconds) {
        long expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        // 使用 playerId + value 作為鍵
        String key = playerId.toString() + ":" + value;
        cooldown_cache.put(key, new CacheEntry<>(value, expireTime));
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
    public static int getCooldown(UUID playerId, String value) {
        // 使用 playerId + value 作為鍵來查找
        String key = playerId.toString() + ":" + value;
        CacheEntry<String> entry = cooldown_cache.get(key);

        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cooldown_cache.remove(key);  // 修正：應該是 cooldown_cache 不是 cache
            }
            return -1;
        }
        return (int) ((entry.getExpireTime() - System.currentTimeMillis()) / 1000);  // 返回秒數
    }
}