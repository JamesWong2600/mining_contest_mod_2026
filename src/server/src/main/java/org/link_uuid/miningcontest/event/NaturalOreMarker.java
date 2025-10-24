package org.link_uuid.miningcontest.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import static org.link_uuid.miningcontest.blockregister.ores.*;

public class NaturalOreMarker {
    public static void init() {
        // 監聽區塊加載事件（用於標記新生成的礦物）
        ServerChunkEvents.CHUNK_GENERATE.register((world, chunk) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                markOresInChunk(world, chunk);
            }
        });

        // 監聽方塊放置事件
        UseBlockCallback.EVENT.register(NaturalOreMarker::onBlockPlace);
    }

    private static void markOresInChunk(World world, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getBottomY(); y < world.getHeight(); y++) {
                    BlockPos pos = new BlockPos(
                            chunkPos.getStartX() + x,
                            y,
                            chunkPos.getStartZ() + z
                    );

                    BlockState state = chunk.getBlockState(pos);

                    if (isCustomOreBlock(state.getBlock())) {
                        // 為自然生成的礦物添加標記
                        NaturalOreStorage.addNaturalOre(pos);
                    }
                }
            }
        }
    }

    private static ActionResult onBlockPlace(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!world.isClient()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (isCustomOreBlock(state.getBlock())) {
                // 玩家放置時移除自然標記
                NaturalOreStorage.removeNaturalOre(pos);
                player.sendMessage(Text.literal("§e放置的礦物將不會計分"), false);
            }
        }
        return ActionResult.PASS;
    }

    private static boolean isCustomOreBlock(Block block) {
        return block.equals(URANIUM_ORE) || block.equals(URANIUM_DEEPSLATE_ORE) ||
                block.equals(LEAD_ORE) || block.equals(LEAD_DEEPSLATE_ORE) ||
                block.equals(IODINE_ORE) || block.equals(IODINE_DEEPSLATE_ORE) ||
                block.equals(DIAMOND_ORE_LEVEL_ONE) ||
                block.equals(DIAMOND_ORE_LEVEL_TWO) || block.equals(DEEPSLATE_DIAMOND_ORE_LEVEL_TWO) ||
                block.equals(DEEPSLATE_DIAMOND_ORE_LEVEL_THREE) ||
                block.equals(Blocks.GOLD_ORE) || block.equals(Blocks.DEEPSLATE_GOLD_ORE) ||
                block.equals(Blocks.IRON_ORE) || block.equals(Blocks.DEEPSLATE_IRON_ORE) ||
                block.equals(Blocks.REDSTONE_ORE) || block.equals(Blocks.DEEPSLATE_REDSTONE_ORE) ||
                block.equals(Blocks.LAPIS_ORE) || block.equals(Blocks.DEEPSLATE_LAPIS_ORE) ||
                block.equals(Blocks.EMERALD_ORE) || block.equals(Blocks.DEEPSLATE_EMERALD_ORE) ||
                block.equals(Blocks.COAL_ORE) || block.equals(Blocks.DEEPSLATE_COAL_ORE) ||
                block.equals(Blocks.COPPER_ORE) || block.equals(Blocks.DEEPSLATE_COPPER_ORE);
    }

    // 檢查是否是自然生成的礦物
    public static boolean isNaturalOre(BlockPos pos) {
        return NaturalOreStorage.isNaturalOre(pos);
    }
}