package org.link_uuid.miningContestMod2026;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import org.link_uuid.miningContestMod2026.event.RadiationHandler;
import org.link_uuid.miningContestMod2026.event.RadiationHandler_old;
import org.link_uuid.miningContestMod2026.packets.RadiationPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;

import static net.minecraft.item.Items.register;
import static org.link_uuid.miningContestMod2026.armor.lead.lead_helmet.lead_equip_init;
import static org.link_uuid.miningContestMod2026.blocks.iodine.iodine_deepslate_ore.iodine_deepslate_ore_init;
import static org.link_uuid.miningContestMod2026.blocks.iodine.iodine_ore.iodine_ore_init;
import static org.link_uuid.miningContestMod2026.blocks.lead.lead_deepslate_ore.lead_deepslate_ore_init;
import static org.link_uuid.miningContestMod2026.blocks.lead.lead_ore.lead_ore_init;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_deepslate_ore.uranium_deepslate_ore;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_deepslate_ore.uranium_deepslate_ore_init;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_ore.*;
import static org.link_uuid.miningContestMod2026.items.element_pickaxe.ModItems.*;

public class MiningContestMod2026 implements ModInitializer {

    public static final String MOD_ID = "mining-contest-mod-2026";
    public static Path CONFIG_DIR;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static int mark = 0;
    public boolean calculated = false;

    // 计分板常量
    String SCOREBOARD_NAME = "my_mod_scores";
    String SCOREBOARD_DISPLAY_NAME = "麥塊夏季挖礦大賽2026工程樣品";
    ScoreboardCriterion criterion = ScoreboardCriterion.DUMMY;  // or another criterion as needed
    Text displayName = Text.of("My Objective");
    ScoreboardCriterion.RenderType renderType = ScoreboardCriterion.RenderType.INTEGER;  // or other render type
    boolean displayAutoUpdate = false;
    NumberFormat numberFormat = null;
    public int tickCounter = 0;

    public static final String SCORE_JOIN_COUNT = "玩家加入次数";
    public static final String SCORE_BLOCKS_MINED = "方块挖掘数";
    public static final String SCORE_MOBS_KILLED = "怪物击杀数";
    public static final String SCORE_ITEMS_CRAFTED = "物品合成数";
    public static final String SCORE_DEATHS = "死亡次数";
    public static final String SCORE_PLAY_TIME = "游戏时间";

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        createConfigDirectory();

        // 載入設定檔
        loadConfig();
        //ModItems.register();
        // Add item to creative tab
        //ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
       //     entries.add(ModItems.ELEMENT_PICKAXE);
       // });
        pickaxe_init();
        uraniumn_init();
        uranium_init();
        lead_init();
        lead_ore_init();
        lead_equip_init();
        iodine_ore_init();
        iodine_init();
        uranium_deepslate_ore_init();
        lead_deepslate_ore_init();
        iodine_deepslate_ore_init();
        //PayloadTypeRegistry.playS2C().register(RadiationPackets.ID, RadiationPackets.CODEC);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            PayloadTypeRegistry.playS2C().register(
                    RadiationPackets.ID,
                    RadiationPackets.CODEC
            );
        }
// In your client-only initializer method


        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining-contest-mod-2026", "uranium_ore_placed")
                )
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining-contest-mod-2026", "lead_ore_placed")
                )
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining-contest-mod-2026", "iodine_ore_placed")
                )
        );

        //ServerLifecycleEvents.SERVER_STARTED.register(this::updateScoreboard);
        ServerTickEvents.END_SERVER_TICK.register(RadiationHandler::onServerTick);
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if(state.getBlock().equals(uraniumn_ore) || state.getBlock().equals(uranium_deepslate_ore) ){
            //player.sendMessage(Text.literal(state.getBlock().getName().getString()+"5分"), false);
                player.sendMessage(Text.literal("鈾原礦+5分").formatted(Formatting.GREEN),false);
                mark+=5;// 播放音效
                player.getWorld().playSound(
                        null, // null = all nearby players hear it
                        player.getBlockPos(),
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );
            }
        });


        /*ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter >= 20) { // 20 ticks = 1 second
                tickCounter = 0;
                updateScoreboard(server);
            }
        });
        */
    }


    private void createConfigDirectory() {
        try {
            Files.createDirectories(CONFIG_DIR);
            System.out.println("Config directory created: " + CONFIG_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + CONFIG_DIR);
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        Path configFile = CONFIG_DIR.resolve("config.json");
        // 你的設定檔載入邏輯
    }

    private void updateScoreboard(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(SCOREBOARD_NAME);

        if (objective == null) {
            objective = scoreboard.addObjective(
                    SCOREBOARD_NAME,
                    ScoreboardCriterion.DUMMY,
                    Text.literal(SCOREBOARD_DISPLAY_NAME).formatted(Formatting.GREEN),
                    ScoreboardCriterion.RenderType.INTEGER,
                    false,
                    null
            );
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
        }

        // 每秒更新“分數”行
        ScoreAccess score = scoreboard.getOrCreateScore(ScoreHolder.fromName("分數:   " + mark), objective);
        score.setScore(0);
    }
}
