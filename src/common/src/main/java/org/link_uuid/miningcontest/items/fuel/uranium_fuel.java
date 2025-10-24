package org.link_uuid.miningcontest.items.fuel;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.*;
import net.minecraft.item.FuelRegistry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import static org.link_uuid.miningcontest.items.item.ores.uranium;

public class uranium_fuel {
        public static void fuel_register(){
                FuelRegistryEvents.BUILD.register((builder, context) -> {
                        builder.add(uranium, 80 * 20);
                });
        }
}
