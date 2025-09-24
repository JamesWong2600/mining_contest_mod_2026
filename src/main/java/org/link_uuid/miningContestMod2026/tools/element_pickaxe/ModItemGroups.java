package org.link_uuid.miningContestMod2026.tools.element_pickaxe;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKeys;

public class ModCreativeTabs {
    public static void register() {
            ItemGroupEvents.modifyEntriesEvent(RegistryKeys.ITEM_GROUP.getOrCreateEntry("minecraft:tools")).register(entries -> {
                entries.add(ModItems.ELEMENT_PICKAXE);
            });
    }
}
