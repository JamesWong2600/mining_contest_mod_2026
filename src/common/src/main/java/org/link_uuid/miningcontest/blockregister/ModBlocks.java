package org.link_uuid.miningcontest.blockregister;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static Block registerBlock(String name, AbstractBlock.Settings settings) {
        // 创建 RegistryKey
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK,
                Identifier.of("mining_contest_mod_2026", name));

        // 创建方块实例并设置 registry key
        Block block = new Block(settings.registryKey(blockKey));

        // 注册方块
        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, block);

        // 创建物品的 RegistryKey
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM,
                Identifier.of("mining_contest_mod_2026", name));

        // 注册对应的方块物品
        Registry.register(Registries.ITEM, itemKey,
                new BlockItem(registeredBlock, new Item.Settings().registryKey(itemKey)));

        return registeredBlock;
    }

}
