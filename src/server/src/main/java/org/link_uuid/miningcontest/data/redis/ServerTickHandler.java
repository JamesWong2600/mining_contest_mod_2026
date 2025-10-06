package org.link_uuid.miningcontest.data.redis;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.link_uuid.miningcontest.data.config.json_init;

public class ServerTickHandler implements ServerTickEvents.StartTick {
    private int updateCounter = 0;

    @Override
    public void onStartTick(net.minecraft.server.MinecraftServer server) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) return;
        if (server.getCurrentPlayerCount() < 1) return;

        // 只在 Redis 可用时使用
        if (!RedisManager.isEnabled()) return;

        updateCounter++;
        if (updateCounter >= 20) { // 每秒执行一次
            RedisService.saveServerPlayerAmount(json_init.config.server_index, server.getCurrentPlayerCount());
            updateCounter = 0;
        }
    }
}

