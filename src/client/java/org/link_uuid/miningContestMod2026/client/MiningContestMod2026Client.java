package org.link_uuid.miningContestMod2026.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.link_uuid.miningContestMod2026.cache.Cacher;
import org.link_uuid.miningContestMod2026.client.HUD.LocalWebServer_useless;
import org.link_uuid.miningContestMod2026.client.HUD.PhoneScreen_useless;
import org.link_uuid.miningContestMod2026.client.HUD.scorecard_UHD;
import org.link_uuid.miningContestMod2026.client.HUD.text_UHD;
import org.link_uuid.miningContestMod2026.event.RadiationHandler;
import org.link_uuid.miningContestMod2026.event.RadiationHandler_old;
import org.link_uuid.miningContestMod2026.packets.RadiationPackets;
import org.objectweb.asm.tree.analysis.Value;

import static org.link_uuid.miningContestMod2026.armor.lead.lead_helmet.*;
import static org.link_uuid.miningContestMod2026.armor.lead.lead_helmet.lead_boot_item;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_ore.uraniumn_ore;
import static org.link_uuid.miningContestMod2026.event.RadiationHandler.RADIUS;
import static org.link_uuid.miningContestMod2026.event.RadiationHandler.distSq;

@Environment(EnvType.CLIENT)
public class MiningContestMod2026Client implements ClientModInitializer {
    private int ticksElapsed = 0; // Counts ticks
    private final int delayTicks = 10; // Delay 10 ticks
    private static float anti_radiation = 0;
    private double nearestDistSq = Double.MAX_VALUE;
    public static float R = 0;
    private CefApp cefApp;
    private CefClient cefClient;
    private CefBrowser cefBrowser;
    private PhoneScreen_useless phoneScreen;
    private KeyBinding openPhoneKey;
    public static float alpha;
    //public static double distance;
    public String s = "";
    public double distance_value;
    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        HudRenderCallback.EVENT.register(new text_UHD());
        HudRenderCallback.EVENT.register(new scorecard_UHD());
        HudRenderCallback.EVENT.register(this::onHudRender);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            PayloadTypeRegistry.playS2C().register(
                    RadiationPackets.ID,
                    RadiationPackets.CODEC
            );
        }

        //HudRenderCallback.EVENT.register(this::renderImage);
        /*openPhoneKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.miningcontestmod.open_phone",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.miningcontestmod"
        ));*/

        LocalWebServer_useless server = new LocalWebServer_useless(8080);
        try {
            server.start();
            System.out.println("NanoHTTPD started at http://localhost:8080/phone.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //MinecraftClient.getInstance().setScreen(new PhoneScreen("Phone GUI"));
        //ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
        //MinecraftClient.getInstance().setScreen(new PhoneScreen(Text.literal("screen")));
    }


    private void renderImage(DrawContext drawContext, RenderTickCounter tickCounter) {
        Identifier IMAGE =
                Identifier.of("mining-contest-mod-2026", "textures/gui/phone.png");
        MinecraftClient client = MinecraftClient.getInstance();
        int x = (int) (client.getWindow().getScaledWidth() * 0.85f);
        int y = (int) (client.getWindow().getScaledHeight() * 0.16f);
        int width = 64; // your image width
        int height = 64; // your image height

        drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, IMAGE, x, y,0,0, 120, 210,120,210);
    }

    public PhoneScreen_useless getPhoneScreen() {
        return phoneScreen;
    }
    /*private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        System.out.println("boolean "+calculated);// only run when main calculation is ready
        if (nearestDistSq == Double.MAX_VALUE) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // Example: draw overlay based on nearest distance
        double dist = Math.sqrt(nearestDistSq);
        float alpha = (float) (1.0 - (dist / RadiationHandler.RADIUS));
        if (alpha <= 0) return;

        int a = (int) (alpha * 200 * RadiationHandler.anti_radiation);
        int color = (a << 24) | (255 << 16) | (255 << 8) | 255; // RGBA
        context.fill(0, 0, width, height, color);

        // reset if you only want to run once
        calculated = false;
    }
*/

    
    /* private void cal(){
        PlayerEntity player = client.player;

    }
*/

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestpalte = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leg = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boot = player.getEquippedStack(EquipmentSlot.FEET);



        if(!client.isInSingleplayer()){
           /* ClientPlayNetworking.registerGlobalReceiver(RadiationPackets.ID, (payload, content) -> {
                content.client().execute(() -> {
                    distance = payload.dist();
                    s = String.valueOf(distance);
                });

            });*/
        }
        else{
                scorecard_UHD.distance[0] = Cacher.get(player.getUuid());
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

        if(scorecard_UHD.distance[0] == -1) return;

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
