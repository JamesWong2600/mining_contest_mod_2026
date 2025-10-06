package org.link_uuid.miningcontest.items.tool;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import org.link_uuid.miningcontest.MiningContestCommon;
import org.link_uuid.miningcontest.items.item.ores;

import java.util.function.Function;

public class tools {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestCommon.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }
    public static final ToolMaterial GUIDITE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            500,
            5F,
            1F,
            22,
            ToolMaterial.DIAMOND.repairItems()
    );

    public static final Item element_pickaxe = register("element_pickaxe", Item::new, new Item.Settings().pickaxe(GUIDITE_TOOL_MATERIAL, 5f, 1f));

    public static void pickaxe_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(tools.element_pickaxe));
    }
}