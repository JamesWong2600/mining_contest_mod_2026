package org.link_uuid.miningcontest.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;

public class ClientDeathEvent {
    private static boolean wasInDeathScreen = false;
    private static boolean respawnRequested = false;

    public static void onClientTick(MinecraftClient client) {
        boolean isInDeathScreen = client.currentScreen instanceof DeathScreen;

        // 偵測進入死亡畫面的瞬間
        if (isInDeathScreen && !wasInDeathScreen) {
            onEnteredDeathScreen(client);
        }

        // 偵測離開死亡畫面
        if (!isInDeathScreen && wasInDeathScreen) {
            onLeftDeathScreen();
        }

        wasInDeathScreen = isInDeathScreen;
    }

    private static void onEnteredDeathScreen(MinecraftClient client) {
        if (!AutoRespawnConfig.ENABLED) return;

        respawnRequested = false;
        System.out.println("進入死亡畫面，準備自動復活");

        // 延遲一段時間後自動復活
        client.send(() -> {
            if (client.currentScreen instanceof DeathScreen && !respawnRequested) {
                performAutoRespawn();
            }
        });
    }

    private static void onLeftDeathScreen() {
        respawnRequested = false;
    }

    private static void performAutoRespawn() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen instanceof DeathScreen && !respawnRequested) {
            respawnRequested = true;

            // 發送復活請求到伺服器
            if (ClientPlayNetworking.canSend(YourMod.AUTO_RESPAWN_PACKET_ID)) {
                ClientPlayNetworking.send(YourMod.AUTO_RESPAWN_PACKET_ID, PacketByteBufs.create());
                System.out.println("從死亡畫面發送自動復活請求");

                // 顯示提示
                client.inGameHud.setOverlayMessage(Text.literal("⚡ 自動復活中..."), false);
            }
        }
    }
}
