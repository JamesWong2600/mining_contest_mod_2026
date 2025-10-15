package org.link_uuid.miningcontest.blockregister.diamond_re_rule;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.*;

public class original_diamond_gen_remove {

    public static void register() {
        // 在區塊載入時移除鑽石礦
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            removeDiamondOresFromChunk(chunk);
        });
    }

    private static void removeDiamondOresFromChunk(WorldChunk chunk) {
        ServerWorld world = (ServerWorld) chunk.getWorld();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        boolean foundDiamond = false;

        // 使用正確的世界高度方法
        int minY = world.getDimension().minY();
        int maxY = world.getDimension().height();

        for (int x = 0; x < 16; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.set(x + chunk.getPos().getStartX(), y, z + chunk.getPos().getStartZ());

                    if (chunk.getBlockState(pos).isOf(net.minecraft.block.Blocks.DIAMOND_ORE) ||
                            chunk.getBlockState(pos).isOf(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE)) {
                        // 替換為石頭
                        chunk.setBlockState(pos, net.minecraft.block.Blocks.STONE.getDefaultState());
                        foundDiamond = true;
                    }
                }
            }
        }

    }
}