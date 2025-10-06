package org.link_uuid.miningcontest.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

public class ores_gen {
    public static void ore_init(){
        // 现在使用已经注册的 placed features
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining_contest_mod_2026", "uranium_ore_placed")
                )
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining_contest_mod_2026", "lead_ore_placed")
                )
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(
                        RegistryKeys.PLACED_FEATURE,
                        Identifier.of("mining_contest_mod_2026", "iodine_ore_placed")
                )
        );
    }
}
