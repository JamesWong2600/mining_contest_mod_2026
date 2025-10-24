package org.link_uuid.miningcontest.countdown;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import org.link_uuid.miningcontest.payload.packets.CountdownPackets;

import static org.link_uuid.miningcontest.HUD.scorecard_UHD.*;

public class ClientCountdownManager_inEnd {
    public static int currentCountdown = -1;
    private static long lastPacketTime = 0;
    public static String text = "";
    public static boolean shouldDisplayFinalMessage = false;
    private static long finalMessageDisplayTime = 0;
    public static int lastCountdownValue = -1;
    public static boolean isInitialized = false;

    public static void end_init() {
        if (isInitialized) return;

        // 註冊 tick 事件（這很重要！）
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            handleCountdown();
        });

        // 註冊 HUD 渲染
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            long currentTime = System.currentTimeMillis();

            // 處理一般倒數顯示
            if (timer_index_score[0] > 0 && timer_index_score[0] <= 10) {
                renderCountdown(drawContext, client);
            }

            // 處理最終訊息顯示
            if (shouldDisplayFinalMessage) {
                long timeSinceFinalMessage = currentTime - finalMessageDisplayTime;
                if (timeSinceFinalMessage > 5000) { // 顯示5秒
                    shouldDisplayFinalMessage = false;
                } else {
                    renderFinalMessage(drawContext, client);
                }
            }
        });

        isInitialized = true;
        System.out.println("ClientCountdownManager_inEnd initialized");
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

    public static void handleCountdown() {
        if (MinecraftClient.getInstance().world == null) return;

        // 更新當前倒數值
        currentCountdown = timer_index_score[0];

        // 如果計時器值沒有變化，就不執行
        if (currentCountdown == lastCountdownValue) {
            return;
        }

        lastCountdownValue = currentCountdown;

        if (currentCountdown == 0 && session_score[0] > 1) {
            // 處理結束訊息
            shouldDisplayFinalMessage = true;
            finalMessageDisplayTime = System.currentTimeMillis();

            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.playSound(
                        SoundEvents.ENTITY_PLAYER_LEVELUP,
                        1.0f, 1.0f
                );
            }
        }

        // 播放倒數音效（只在特定範圍內）
        if (currentCountdown <= 10 && currentCountdown > 0 && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.playSound(
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    1.0f,
                    1.0f + (10 - currentCountdown) * 0.1f
            );
        }

        text = formatCountdownText(currentCountdown);
        System.out.println("Countdown: " + currentCountdown + " at " + System.currentTimeMillis());
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

    private static String formatCountdownText(int countdown) {
        if (countdown == 0) {
            return "比賽結束，你的最終得分為" + score_score[0];
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

}