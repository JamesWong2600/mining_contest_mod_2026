package org.link_uuid.miningContestMod2026.anti_xray;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.block.BlockState;
//import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.WorldChunk;
import org.link_uuid.miningContestMod2026.items.element_pickaxe.ModItems;

import static org.link_uuid.miningContestMod2026.blocks.lead.lead_ore.lead_ore;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_ore.uraniumn_ore;

public class ChunkPacketInterceptor {

    // 修改：直接傳入 chunk 和玩家
    public static ChunkDataS2CPacket processChunkPacket(WorldChunk chunk, ServerPlayerEntity player) {
        World world = player.getWorld();
        ChunkPos chunkPos = chunk.getPos();

        // 創建區塊的副本來修改，避免影響真實世界
        WorldChunk modifiedChunk = createModifiedChunkCopy(chunk, world, player);

        // 返回修改後的封包（這裡需要實際實現封包創建）
        return createChunkPacket(modifiedChunk, world);
    }

    private static WorldChunk createModifiedChunkCopy(WorldChunk original, World world, ServerPlayerEntity player) {
        // 這裡需要實現區塊深拷貝和修改邏輯
        // 這是一個複雜的操作，需要複製整個區塊數據
        ChunkPos chunkPos = original.getPos();

        int minY = world.getDimension().minY();  // 世界最低高度
        int height = world.getDimension().height(); // 世界總高度
        int maxY = minY + height; // 計算最高高度
        // 遍歷並修改方塊

        for (int x = 0; x < 16; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(
                            chunkPos.getStartX() + x,
                            y,
                            chunkPos.getStartZ() + z
                    );
                    BlockState originalState = original.getBlockState(pos);

                    if (isHiddenOre(originalState) && !isExposed(world, pos)) {
                        // 在副本中設置石頭
                        // 注意：這裡需要實際實現區塊拷貝
                    }
                }
            }
        }

        return original; // 返回修改後的副本
    }

    // 其餘方法不變...
    private static boolean isHiddenOre(BlockState state) {
        return state.isOf(Blocks.DIAMOND_ORE)
                || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.isOf(uraniumn_ore)
                || state.isOf(lead_ore);
    }

    private static boolean isExposed(World world, BlockPos pos) {
        for (BlockPos adjacent : BlockPos.iterate(
                pos.add(-1, -1, -1),
                pos.add(1, 1, 1)
        )) {
            if (world.getBlockState(adjacent).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static ChunkDataS2CPacket createChunkPacket(WorldChunk chunk, World world) {
        // 實際創建封包的邏輯
        return null; // 需要實現
    }
}