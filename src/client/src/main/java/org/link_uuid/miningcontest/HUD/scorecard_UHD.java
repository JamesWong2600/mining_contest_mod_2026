package org.link_uuid.miningcontest.HUD;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.link_uuid.miningcontest.payload.packets.*;

import static org.link_uuid.miningcontest.MiningContestClient.R;

public class scorecard_UHD implements HudRenderCallback {
    private static float anti_radiation = 0;
    public static double[] distance = new double[1];
    public static double[] distance_score = new double[1];
    public static int[] session = new int[1];
    public static int[] session_score = new int[1];
    public static int[] pings = new int[1];
    public static int[] pings_score = new int[1];
    public static String[] player_amount = new String[1];
    public static String[] player_amount_score = new String[1];
    public static int[] mspt = new int[1];
    public static int[] mspt_score = new int[1];
    public static int[] mode_index = new int[1];
    public static int[] mode_index_score = new int[1];
    public static int[] timer_index = new int[1];
    public static int[] timer_index_score = new int[1];
    public static int[] score = new int[1];
    public static int[] score_score = new int[1];
    public static String[] mode_string = new String[1];
    public static Text[] lines = new Text[10];

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo serverInfo = client.getCurrentServerEntry();
        //if (serverInfo != null) {
        //     pings[0] = (int) serverInfo.ping;
        // }
        TextRenderer textRenderer = client.textRenderer;
        PlayerEntity player = client.player;
        //MiningContestMod2026Client.distance = Cacher.get(player.getUuid());
        //player.sendMessage(Text.literal(String.valueOf(pings[0])), false);
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

        ClientPlayNetworking.registerGlobalReceiver(SessionPackets.ID,
                (payload, context) -> {
                    session[0] = payload.session();
                    context.client().execute(() -> {
                        session_score[0] = session[0];
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(PingPackets.ID,
                (payload, context) -> {
                    pings[0] = payload.ping();
                    context.client().execute(() -> {
                        pings_score[0] = pings[0];
                        //player.sendMessage(Text.literal(String.valueOf(pings_score[0])), false);
                    });

                }
        );

        ClientPlayNetworking.registerGlobalReceiver(MsptPackets.ID,
                (payload, context) -> {
                    mspt[0] = payload.mspt();
                    context.client().execute(() -> {
                        mspt_score[0] = mspt[0];
                    });

                }
        );

        ClientPlayNetworking.registerGlobalReceiver(PlayerAmountPackets.ID,
                (payload, context) -> {
                    player_amount[0] = payload.playeramount();
                    context.client().execute(() -> {
                        player_amount_score[0] = player_amount[0];
                    });

                }
        );

        ClientPlayNetworking.registerGlobalReceiver(PVPModePacket.ID,
                (payload, context) -> {
                    mode_index[0] = payload.mode_index();
                    context.client().execute(() -> {
                        mode_index_score[0] = mode_index[0];
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(TimerPackets.ID,
                (payload, context) -> {
                    timer_index[0] = payload.timer();
                    context.client().execute(() -> {
                        timer_index_score[0] = timer_index[0];
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(ScorePackets.ID,
                (payload, context) -> {
                    score[0] = payload.score();
                    context.client().execute(() -> {
                        score_score[0] = score[0];
                    });
                }
        );
        //player.sendMessage(Text.of("AAA"+distance_score[0]),false);

        if (mode_index_score[0] == 0){
            mode_string[0] = "和平模式";
        }else{
            mode_string[0] = "戰鬥模式";
        }

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
        //String ping = "45";
        String mspt = "15";

        // Use fixed-width formatting

        if(session_score[0] == 2){
            lines = new Text[]{
                    // Header - gold and bold
                    (Text) Text.literal("麥塊新春挖礦大賽2026").formatted(Formatting.GOLD, Formatting.BOLD),

                    (Text) createLine("目前狀態: ", "正在比賽", "", Formatting.GREEN),
                    // Remaining time - label in white, value in green, unit in gray
                    (Text) createLine("剩餘時間: ", String.valueOf(timer_index_score[0]), " s", Formatting.GREEN),

                    // Your score - label in white, value in yellow, unit in gray
                    (Text)  createLine("你的分數: ", String.valueOf(score_score[0]), " 分", Formatting.GREEN),

                    // Player count - label in white, value in aqua, unit in gray
                    (Text) createLine("玩家數量: ", String.valueOf(player_amount_score[0]), " 人", Formatting.GREEN),

                    (Text) createLine("當前環境輻射值: ", String.format("%.2f", distance_score[0]), " Sv", Formatting.GREEN),

                    (Text) createLine("當前身體倫琴值: ", String.format("%.2f", R), " R", Formatting.GREEN),

                    (Text)  createLine("輻射防護能力: ", String.valueOf(pings_score[0]), " %", Formatting.GREEN),
                    // Ping - label in white, value in light purple, unit in gray
                    (Text) createLine("ping: ", String.valueOf(pings_score[0]), " ms", getPingColor(pings_score[0])),

                    // MSPT - label in white, value in red, unit in gray
                    (Text) createLine("mspt: ", String.valueOf(mspt_score[0]), " ms", Formatting.GREEN),

            };
        }
        else if(session_score[0] == 3){
            lines = new Text[]{
                    // Header - gold and bold
                    (Text) Text.literal("麥塊新春挖礦大賽2026").formatted(Formatting.GOLD, Formatting.BOLD),

                    (Text) createLine("目前狀態: ", "比賽已結束", "", Formatting.GREEN),
                    // Your score - label in white, value in yellow, unit in gray
                    (Text)  createLine("你的最終分數: ", String.valueOf(score_score[0]), " 分", Formatting.GREEN),

                    // Player count - label in white, value in aqua, unit in gray
                    (Text) createLine("玩家數量: ", String.valueOf(player_amount_score[0]), " 人", Formatting.GREEN),

                    // Ping - label in white, value in light purple, unit in gray
                    (Text) createLine("ping: ", String.valueOf(pings_score[0]), " ms", getPingColor(pings_score[0])),

                    // MSPT - label in white, value in red, unit in gray
                    (Text) createLine("mspt: ", String.valueOf(mspt_score[0]), " ms", Formatting.GREEN),

            };
        }
        else{
            lines = new Text[]{
                    // Header - gold and bold
                    (Text) Text.literal("麥塊新春挖礦大賽2026").formatted(Formatting.GOLD, Formatting.BOLD),

                    // Remaining time - label in white, value in green, unit in gray
                    (Text) createLine("目前狀態: ", "請等待比賽開始", "", Formatting.GREEN),

                    // Player count - label in white, value in aqua, unit in gray
                    (Text) createLine("玩家數量: ", String.valueOf(player_amount_score[0]), " 人", Formatting.GREEN),
                    // Ping - label in white, value in light purple, unit in gray
                    (Text) createLine("ping: ", String.valueOf(pings_score[0]), " ms", getPingColor(pings_score[0])),

                    // MSPT - label in white, value in red, unit in gray
                    (Text) createLine("mspt: ", String.valueOf(mspt_score[0]), " ms", Formatting.GREEN),
                    (Text) createLine("大廳PVP模式: ", mode_string[0], "", Formatting.GREEN),

            };
        }


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
                Identifier.of("mining_contest_mod_2026", "textures/gui/phone.png");
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

    public static Formatting getPingColor(int ping) {
        if (ping == 0) return Formatting.GRAY;
        if (ping < 50) return Formatting.GREEN;
        if (ping < 100) return Formatting.YELLOW;
        if (ping < 200) return Formatting.GOLD;
        return Formatting.RED;
    }
}