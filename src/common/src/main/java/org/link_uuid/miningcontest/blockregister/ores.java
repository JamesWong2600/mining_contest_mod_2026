package org.link_uuid.miningcontest.blockregister;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import net.minecraft.sound.BlockSoundGroup;


import static org.link_uuid.miningcontest.blockregister.ModBlocks.registerBlock;

public class ores {  // 类名改为大写
    public static final Block URANIUM_ORE = registerBlock("uranium_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );

    public static final Block URANIUM_DEEPSLATE_ORE = registerBlock("uranium_deepslate_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );

    public static final Block IODINE_DEEPSLATE_ORE = registerBlock("iodine_deepslate_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );

    public static final Block IODINE_ORE = registerBlock("iodine_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );


    public static final Block LEAD_DEEPSLATE_ORE = registerBlock("lead_deepslate_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );

    public static final Block LEAD_ORE = registerBlock("lead_ore",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.ANVIL)
    );

    public static void registerToItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS)
                .register(entries -> {
                    entries.add(URANIUM_ORE);
                    entries.add(IODINE_ORE);
                    entries.add(LEAD_ORE);
                    entries.add(URANIUM_DEEPSLATE_ORE);
                    entries.add(IODINE_DEEPSLATE_ORE);
                    entries.add(LEAD_DEEPSLATE_ORE);
                });
    }
}