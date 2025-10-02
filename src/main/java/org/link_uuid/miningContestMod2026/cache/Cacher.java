package org.link_uuid.miningContestMod2026.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cacher<K, V> {
    private static final Map<UUID, Double> cache = new HashMap<>();

    public static void put(UUID playerId, double distance) {
        cache.put(playerId, distance);
    }

    public static double get(UUID playerId) {
        return cache.getOrDefault(playerId, -1.0);
    }

    public static boolean contains(UUID playerId) {
        return cache.containsKey(playerId);
    }

    public static void remove(UUID playerId) {
        cache.remove(playerId);
    }
}