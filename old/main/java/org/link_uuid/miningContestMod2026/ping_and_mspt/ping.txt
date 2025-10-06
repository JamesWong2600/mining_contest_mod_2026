package org.link_uuid.miningContestMod2026.ping_and_mspt;

import net.minecraft.server.network.ServerPlayerEntity;

public class ping {


    // 安全獲取 Ping（檢查是否為單人遊戲）
    public static int getPingSafe(ServerPlayerEntity player) {
        if (player.getServer().isSingleplayer()) {
            System.out.println("sing sing");
            return 0; // 單人遊戲返回 0
        }
        return player.networkHandler.getLatency();
    }

    // 獲取 Ping 狀態描述
    // 獲取 Ping 顏色

}
