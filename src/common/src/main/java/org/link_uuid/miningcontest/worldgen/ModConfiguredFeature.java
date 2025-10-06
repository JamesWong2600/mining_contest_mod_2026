package org.link_uuid.miningcontest.worldgen;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.link_uuid.miningcontest.blockregister.ores;

import java.util.List;

public class ModConfiguredFeature{

    public static final RegistryKey<ConfiguredFeature<?, ?>> URANIUM_ORE_KEY = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "uranium_ore"));
    public static final RegistryKey<ConfiguredFeature<?, ?>> LEAD_ORE_KEY = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "lead_ore"));
    public static final RegistryKey<ConfiguredFeature<?, ?>> IODINE_ORE_KEY = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "iodine_ore"));

    // 放置的 Feature Keys
    public static final RegistryKey<PlacedFeature> URANIUM_ORE_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "uranium_ore_placed"));
    public static final RegistryKey<PlacedFeature> LEAD_ORE_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "lead_ore_placed"));
    public static final RegistryKey<PlacedFeature> IODINE_ORE_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE,
            Identifier.of("data/mining_contest_mod_2026", "iodine_ore_placed"));

    // 注册配置的 Features
    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // 铀矿石配置
        register(context, URANIUM_ORE_KEY, Feature.ORE, new OreFeatureConfig(
                List.of(
                        OreFeatureConfig.createTarget(stoneReplaceables, ores.URANIUM_ORE.getDefaultState()),
                        OreFeatureConfig.createTarget(deepslateReplaceables, ores.URANIUM_ORE.getDefaultState())
                ),
                9 // 矿脉大小
        ));

        // 铅矿石配置
        register(context, LEAD_ORE_KEY, Feature.ORE, new OreFeatureConfig(
                List.of(
                        OreFeatureConfig.createTarget(stoneReplaceables, ores.LEAD_ORE.getDefaultState()),
                        OreFeatureConfig.createTarget(deepslateReplaceables, ores.LEAD_ORE.getDefaultState())
                ),
                7 // 矿脉大小
        ));

        // 碘矿石配置
        register(context, IODINE_ORE_KEY, Feature.ORE, new OreFeatureConfig(
                List.of(
                        OreFeatureConfig.createTarget(stoneReplaceables, ores.IODINE_ORE.getDefaultState()),
                        OreFeatureConfig.createTarget(deepslateReplaceables, ores.IODINE_ORE.getDefaultState())
                ),
                5 // 矿脉大小
        ));
    }

    private static <FC extends OreFeatureConfig, F extends Feature<FC>> void register(
            Registerable<ConfiguredFeature<?, ?>> context,
            RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
