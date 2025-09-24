package com.example.mymod.item;

import net.minecraft.item.Item;
import net.minecraft.item.Tier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.link_uuid.miningContestMod2026.tools.element_pickaxe.ModToolMaterials;

public class ModItems {
    public static final Item CUSTOM_PICKAXE = new CustomPickaxeItem(
            ModToolMaterials.ELEMENT,
            new Item.Settings().group(ItemGroup.TOOLS)
    );

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("mymod", "custom_pickaxe"), CUSTOM_PICKAXE);
    }
}
