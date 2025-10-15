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


    public static final Block DEEPSLATE_DIAMOND_ORE_LEVEL_ONE = registerBlock("deepslate_diamond_ore_level_one",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
    );
    public static final Block DEEPSLATE_DIAMOND_ORE_LEVEL_TWO = registerBlock("deepslate_diamond_ore_level_two",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
    );
    public static final Block DEEPSLATE_DIAMOND_ORE_LEVEL_THREE = registerBlock("deepslate_diamond_ore_level_three",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
    );
    public static final Block DIAMOND_ORE_LEVEL_ONE = registerBlock("diamond_ore_level_one",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
    );
    public static final Block DIAMOND_ORE_LEVEL_TWO = registerBlock("diamond_ore_level_two",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
    );
    public static final Block DIAMOND_ORE_LEVEL_THREE = registerBlock("diamond_ore_level_three",
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.STONE)
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
                    entries.add(DEEPSLATE_DIAMOND_ORE_LEVEL_ONE);
                    entries.add(DEEPSLATE_DIAMOND_ORE_LEVEL_TWO);
                    entries.add(DEEPSLATE_DIAMOND_ORE_LEVEL_THREE);
                    entries.add(DIAMOND_ORE_LEVEL_ONE);
                    entries.add(DIAMOND_ORE_LEVEL_TWO);
                    entries.add(DIAMOND_ORE_LEVEL_THREE);
                });
    }
}