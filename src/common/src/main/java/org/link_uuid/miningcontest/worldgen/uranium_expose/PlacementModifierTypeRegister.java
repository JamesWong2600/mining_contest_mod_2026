package org.link_uuid.miningcontest.worldgen.uranium_expose;


import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class PlacementModifierTypeRegister {
    public static final PlacementModifierType<ExposedOrePlacement> EXPOSED_ORE_PLACEMENT =
            () -> (com.mojang.serialization.MapCodec<ExposedOrePlacement>) ExposedOrePlacement.CODEC;

    public static void register() {
        Registry.register(Registries.PLACEMENT_MODIFIER_TYPE,
                Identifier.of("mining_contest_mod_2026", "exposed_ore"),
                EXPOSED_ORE_PLACEMENT);
    }
}