package org.link_uuid.miningContestMod2026.tools.element_pickaxe;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;

public class ModToolMaterials {
    public static final TagKey<Block> ELEMENT_INCORRECT_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of("miningcontestmod2026", "element_incorrect_blocks"));

    // Example: item tag
    public static final TagKey<Item> ELEMENT_REPAIR_ITEMS =
            TagKey.of(RegistryKeys.ITEM, Identifier.of("miningcontestmod2026", "element_repair_items"));

    public static final ToolMaterial ELEMENT = new ToolMaterial(
            ELEMENT_INCORRECT_BLOCKS,
            1500,
            8.0f,
            3.0f,
            22,
            ELEMENT_REPAIR_ITEMS
    );
}