package org.link_uuid.miningContestMod2026.client.HUD;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.link_uuid.miningContestMod2026.MiningContestMod2026;
import org.link_uuid.miningContestMod2026.cache.Cacher;
import org.link_uuid.miningContestMod2026.client.MiningContestMod2026Client;
import org.link_uuid.miningContestMod2026.packets.RadiationPackets;

import static org.link_uuid.miningContestMod2026.armor.lead.lead_helmet.*;
import static org.link_uuid.miningContestMod2026.client.MiningContestMod2026Client.R;

public class scorecard_UHD implements HudRenderCallback {
    private static float anti_radiation = 0;
    public static double[] distance = new double[1];
    public static double[] distance_score = new double[1];
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        PlayerEntity player = client.player;
        //MiningContestMod2026Client.distance = Cacher.get(player.getUuid());

        ClientPlayNetworking.registerGlobalReceiver(RadiationPackets.ID,
                (payload, context) -> {
                    distance[0] = payload.dist();
                    context.client().execute(() -> {
                        // 在 client thread 更新數據
                        distance_score[0] = distance[0];

                        //System.out.println("收到伺服端距離: " + s);
                    });
                }
        );

        //player.sendMessage(Text.of("AAA"+distance_score[0]),false);

        if(distance[0] == -1){
            distance_score[0] = 0;
        }else{
            if(distance[0] > 0){
            R = R+ ((float) (1/distance[0]));
            }
            distance_score[0] = 1/distance[0];
        }
        /*if(!client.isInSingleplayer()) {
        ClientPlayNetworking.registerGlobalReceiver(RadiationPackets.ID,
                (payload, context) -> {
                    MiningContestMod2026Client.distance = payload.dist();
                    context.client().execute(() -> {
                        if(MiningContestMod2026Client.distance == -1){
                            distance_score[0] = String.valueOf(0);
                        }else{
                            R = R+ ((float) (1/MiningContestMod2026Client.distance));
                            distance_score[0] = String.valueOf(1/MiningContestMod2026Client.distance);
                        }
                    });
                }
        );
        }*/


        String remainingTime = "120";
        String yourScore = "1500";
        String playerCount = "24";
        String ping = "45";
        String mspt = "15";

        // Use fixed-width formatting
        Text[] lines = {
                // Header - gold and bold
                Text.literal("麥塊新春挖礦大賽2026").formatted(Formatting.GOLD, Formatting.BOLD),

                // Remaining time - label in white, value in green, unit in gray
                createLine("剩餘時間: ", String.valueOf(remainingTime), " s", Formatting.GREEN),

                // Your score - label in white, value in yellow, unit in gray
                createLine("你的分數: ", String.valueOf(MiningContestMod2026.mark), " 分", Formatting.GREEN),

                // Player count - label in white, value in aqua, unit in gray
                createLine("玩家數量: ", String.valueOf(playerCount), " 人", Formatting.GREEN),

                createLine("當前環境輻射值: ", String.format("%.2f", distance_score[0]), " Sv", Formatting.GREEN),

                createLine("當前身體倫琴值: ", String.format("%.2f", R), " R", Formatting.GREEN),

                createLine("輻射防護能力: ", String.valueOf(ping), " %", Formatting.GREEN),
                // Ping - label in white, value in light purple, unit in gray
                createLine("ping: ", String.valueOf(ping), " ms", Formatting.GREEN),

                // MSPT - label in white, value in red, unit in gray
                createLine("mspt: ", String.valueOf(mspt), " ms", Formatting.GREEN),

        };

        // Position calculation
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int maxWidth = 0;
        for (Text line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(line));
        }

        int x = screenWidth - maxWidth - 5;
        int y = screenHeight / 2 - (lines.length * 6);

        // Draw background
        drawContext.fill(x - 5, y - 5, x + maxWidth + 5, y + (lines.length * 12), 0x80000000);

        // Draw text
        for (int i = 0; i < lines.length; i++) {
            int textY = y + (i * 12);
            if (i == 0) {
                // Center the first line within the background
                int centerX = x + (maxWidth / 2) - (textRenderer.getWidth(lines[i]) / 2);
                drawContext.drawText(textRenderer, lines[i], centerX, textY, 0xFF00FF00, false);
            } else {
                // Other lines remain left-aligned
                drawContext.drawText(textRenderer, lines[i], x, textY, 0xFF00FF00, false);
            }
        }

       /* for (int i = 0; i < lines.length; i++) {
            //System.out.println(lines[i]);
            drawContext.drawText(textRenderer, lines[i], x, y + (i * 12), 0xFF00FF00, false);
        }
        */
        /*Identifier IMAGE =
                Identifier.of("mining-contest-mod-2026", "textures/gui/phone.png");
        int phone_x = (int) (client.getWindow().getScaledWidth() * 0.83f);
        int phone_y = (int) (client.getWindow().getScaledHeight() * 0.16f);
        int width = 64; // your image width
        int height = 64; // your image height
        drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, IMAGE, phone_x, phone_y,0,0, 120, 210,120,210);

        int color = 0xFF000000; // White
        int score_color = 0xFF00FF00; ; // White
        title_text = "麥塊新春挖礦大賽2026";
        int x3 = (int) (client.getWindow().getScaledWidth() * 0.845f);
        int y3 = (int) (client.getWindow().getScaledHeight() * 0.23f);
        drawContext.drawText(scoreRenderer, title_text, x3, y3, color, false);
        score_text = "分數: " + mark;
        int x2 = (int) (client.getWindow().getScaledWidth() * 0.9f);
        int y2 = (int) (client.getWindow().getScaledHeight() * 0.31f);
        drawContext.drawText(scoreRenderer, score_text, x2, y2, color, false);
        // Calculate center X
        int x = client.getWindow().getScaledWidth() / 2 - textRenderer.getWidth(text) / 2;
        int y = (int) (client.getWindow().getScaledHeight() * 0.8f);
        anti_radiation = 0;
        drawContext.drawText(textRenderer, text, x, y, score_color, false);
         */
    }
    private static Text createLine(String label, String value, String unit, Formatting valueColor) {
        return Text.literal(label).formatted(Formatting.WHITE)
                .append(Text.literal(value).formatted(valueColor))
                .append(Text.literal(unit).formatted(Formatting.GRAY));
    }
}