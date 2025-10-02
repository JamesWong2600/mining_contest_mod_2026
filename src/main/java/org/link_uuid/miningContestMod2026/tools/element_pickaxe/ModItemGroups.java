package org.link_uuid.miningContestMod2026.tools.element_pickaxe;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKeys;

public class ModItemGroups {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            //entries.add(ModItems.ELEMENT_PICKAXE);
        });
    }
}
