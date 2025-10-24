package org.link_uuid.miningcontest.countdown;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import org.link_uuid.miningcontest.payload.packets.CountdownPackets;
import org.link_uuid.miningcontest.payload.packets.MsptPackets;

import static org.link_uuid.miningcontest.HUD.scorecard_UHD.*;

public class ClientCountdownManager_onstart {
    private static int currentCountdown = -1;
    private static long lastPacketTime = 0;
    public static String text = "";
    private static boolean shouldDisplayFinalMessage = false;
    private static long finalMessageDisplayTime = 0;
    private static int lastProcessedCountdown = -1; // 防止重複處理

    public static void init() {
        // 註冊 HUD 渲染
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            long currentTime = System.currentTimeMillis();

            // 處理一般倒數顯示 (只顯示 1-10)
            if (currentCountdown > 0 && currentCountdown <= 10) {
                renderCountdown(drawContext, client);
            }

            // 處理最終訊息顯示 (倒數為 0 時)
            if (shouldDisplayFinalMessage) {
                long timeSinceFinalMessage = currentTime - finalMessageDisplayTime;
                if (timeSinceFinalMessage > 5000) { // 顯示5秒
                    shouldDisplayFinalMessage = false;
                    currentCountdown = -1; // 重置倒數
                } else {
                    renderFinalMessage(drawContext, client);
                }
            }
        });

        // 註冊封包處理器
        ClientPlayNetworking.registerGlobalReceiver(CountdownPackets.ID, (payload, context) -> {
            int countdownValue = payload.countdown();
            long packetTimestamp = payload.timestamp();
            int zero_session = payload.session();
            context.client().execute(() -> {
                if (session_score[0] == 1){
                    handleCountdownPacket(countdownValue, packetTimestamp);
                }
            });
        });
    }

    private static void handleCountdownPacket(int countdownValue, long packetTimestamp) {
        // 防止重複處理相同的倒數值
        if (countdownValue == lastProcessedCountdown) {
            return;
        }
        lastProcessedCountdown = countdownValue;

        lastPacketTime = packetTimestamp;

        if (countdownValue == 0) {
            // 處理開始訊息
            shouldDisplayFinalMessage = true;
            finalMessageDisplayTime = packetTimestamp;
            currentCountdown = 0; // 設定為 0

            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.playSound(
                        SoundEvents.ENTITY_PLAYER_LEVELUP,
                        1.0f, 1.0f
                );
            }
        } else {
            // 處理一般倒數
            currentCountdown = countdownValue;
            shouldDisplayFinalMessage = false; // 確保不會同時顯示
        }

        // 播放倒數音效 (只在 1-10 且不是重複值時)
        if (countdownValue <= 10 && countdownValue > 0 && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.playSound(
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    1.0f,
                    1.0f + (10 - countdownValue) * 0.1f
            );
        }

        text = formatCountdownText(countdownValue);
        System.out.println("Countdown: " + countdownValue + " at " + packetTimestamp);
    }

    private static void renderCountdown(DrawContext context, MinecraftClient client) {
        if (text == null || text.isEmpty() || currentCountdown == 0) return;

        float scale = 3.0f;
        int textWidth = (int) (client.textRenderer.getWidth(text) * scale);
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 3;

        int color = getCountdownColor(currentCountdown);

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);

        int scaledX = (int) (x / scale);
        int scaledY = (int) (y / scale);

        context.drawText(client.textRenderer, text, scaledX, scaledY, color, true);
        context.getMatrices().pop();
    }

    private static void renderFinalMessage(DrawContext context, MinecraftClient client) {
        String finalText = formatCountdownText(0);
        if (finalText == null || finalText.isEmpty()) return;

        float scale = 2.5f;
        int textWidth = (int) (client.textRenderer.getWidth(finalText) * scale);
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2;

        int color = 0x00FF00; // 綠色

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);

        int scaledX = (int) (x / scale);
        int scaledY = (int) (y / scale);

        context.drawText(client.textRenderer, finalText, scaledX, scaledY, color, true);
        context.getMatrices().pop();
    }

    private static String formatCountdownText(int countdown) {
        if (countdown == 0) {
            return "比賽開始!";
        }
        return String.valueOf(countdown);
    }

    private static int getCountdownColor(int countdown) {
        return switch (countdown) {
            case 10, 9, 8 -> 0xFFFFFF; // 白色
            case 7, 6, 5 -> 0xFFFF00; // 黃色
            case 4, 3 -> 0xFFA500; // 橙色
            case 2, 1 -> 0xFF0000; // 紅色
            default -> 0xFFFFFF; // 白色
        };
    }

    // 重置方法
    public static void reset() {
        currentCountdown = -1;
        lastProcessedCountdown = -1;
        shouldDisplayFinalMessage = false;
        text = "";
        System.out.println("ClientCountdownManager_onstart reset");
    }
}