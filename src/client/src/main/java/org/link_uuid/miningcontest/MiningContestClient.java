package org.link_uuid.miningcontest;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.link_uuid.miningcontest.HUD.scorecard_UHD;
import org.link_uuid.miningcontest.HUD.text_UHD;

import javax.swing.text.JTextComponent;

import static com.mojang.authlib.minecraft.client.MinecraftClient.*;
import static org.link_uuid.miningcontest.items.armor.lead_armor.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;


public class MiningContestClient implements ClientModInitializer {
    private int ticksElapsed = 0; // Counts ticks
    private final int delayTicks = 10; // Delay 10 ticks
    private static float anti_radiation = 0;
    private double nearestDistSq = Double.MAX_VALUE;
    public static float R = 0;
    private CefApp cefApp;
    private CefClient cefClient;
    private CefBrowser cefBrowser;
    public static float alpha;
    //public static double distance;
    public String s = "";
    public double distance_value;
    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        System.out.println("Mining Contest Client Mod 初始化完成!");
        PlayerEntity player = client.player;
        HudRenderCallback.EVENT.register(new text_UHD());
        HudRenderCallback.EVENT.register(new scorecard_UHD());
        HudRenderCallback.EVENT.register(this::onHudRender);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client_2) -> {
            if (isSingleplayerWorld(client_2)) {
                blockSingleplayerAccess(client_2);
            }
        });

        // 可选：在标题屏幕显示警告
        ScreenEvents.AFTER_INIT.register((client_2, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof net.minecraft.client.gui.screen.TitleScreen) {
                showSingleplayerWarning();
            }
        });
    }

    private boolean isSingleplayerWorld(MinecraftClient client) {
        return client.getServer() != null && client.isInSingleplayer();
    }

    private void blockSingleplayerAccess(MinecraftClient client_2) {
        // 切换到阻止屏幕
        net.minecraft.client.gui.screen.Screen disconnectScreen =
                new net.minecraft.client.gui.screen.MessageScreen(
                        Text.literal("§4✖ 单机模式不可用")
                                .append(Text.literal("\n\n"))
                                .append(Text.literal("§fMining Contest Mod 2026\n"))
                                .append(Text.literal("§7这个模组专为多人游戏设计\n"))
                                .append(Text.literal("§7请连接到支持的服务器来体验完整功能"))
                );

        // 可选：强制断开连接
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 等待3秒
                client_2.execute(() -> {
                    if (client_2.world != null) {
                        client_2.world.disconnect();
                    }
                    client_2.disconnect(disconnectScreen,false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSingleplayerWarning() {
        System.out.println("警告：此模组不支持单机模式！");
    }
    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestpalte = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leg = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boot = player.getEquippedStack(EquipmentSlot.FEET);



        if(client.isInSingleplayer()){
            scorecard_UHD.distance[0] = 0;
            s = String.valueOf(scorecard_UHD.distance[0]);
        }

        //player.sendMessage(Text.literal(player.getUuid() + " " + s),false);
        // Check if it's a golden helmet
        if (helmet.getItem() == lead_helmet_item) {
            anti_radiation+= 0.2F;
        }
        if (chestpalte.getItem() == lead_chestplate_item) {
            anti_radiation+= 0.4F;
        }
        if (leg.getItem() == lead_legging_item) {
            anti_radiation+= 0.2F;
        }
        if (boot.getItem() == lead_boot_item) {
            anti_radiation+= 0.1F;
        }

        if (client.player == null || client.world == null) return;

        if(scorecard_UHD.distance[0] == -1 || scorecard_UHD.distance[0] == 0.0) {
            return;
        };

        alpha = (float) (1 / scorecard_UHD.distance[0]);
        if (alpha <= 0) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        int a = (int) (alpha * 50 * (1 - anti_radiation)); // 最大 128 的透明度
        int color = (a << 24) | (255 << 16) | (255 << 8);
        context.fill(0, 0, width, height, color);

        int particleCount = 300; // 粒子數量更多
        int size = 6; // 粒子尺寸
        for (int i = 0; i < particleCount; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            // 隨機漂浮
            int dx = (int) (Math.random() * 3 - 1);
            int dy = (int) (Math.random() * 3 - 1);

            int brightness = 100 + (int)(Math.random() * 155); // 亮度變化
            int color2 = (a << 24) | (brightness << 16) | (brightness << 8);

            context.fill(x + dx, y + dy, x + dx + size, y + dy + size, color2);
        }
        anti_radiation = 0;
    }

}