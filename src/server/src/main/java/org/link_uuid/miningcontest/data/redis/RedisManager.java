package org.link_uuid.miningcontest.data.redis;

import org.apache.logging.log4j.LogManager;
import org.link_uuid.miningcontest.data.config.json_init;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import org.apache.logging.log4j.Logger;

public class RedisManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static JedisPool jedisPool;
    private static boolean enabled = false;

    public static void initialize() {
        try {
            // Redis 连接配置
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setMaxWait(Duration.ofSeconds(30));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            // 创建连接池
            jedisPool = new JedisPool(poolConfig, json_init.config.redisHost, json_init.config.redisPort, 2000);

            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                enabled = true;
                LOGGER.info("Redis 连接成功");
            }

        } catch (Exception e) {
            LOGGER.info("Redis 连接失败: " + e.getMessage());
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Jedis getResource() {
        return jedisPool.getResource();
    }

    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            LOGGER.info("Redis 连接池已关闭");
        }
    }
}