package org.link_uuid.miningcontest.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.data.variable.variable;
import org.link_uuid.miningcontest.payload.packets.PVPModePacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.link_uuid.miningcontest.MiningContestServer.randomInt;
import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.data.cache.Cache.setCooldown;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class PlayerDeadEvent {

    public static void instantRespawn(ServerPlayerEntity deadPlayer) {
        try {
            ServerPlayerEntity oldPlayer = deadPlayer;
            deadPlayer.networkHandler.sendPacket(new CloseScreenS2CPacket(1));
            ServerPlayerEntity newPlayer = server.getPlayerManager().respawnPlayer(oldPlayer, false, Entity.RemovalReason.KILLED);
            System.out.println("已執行復活: " + newPlayer.getName().getString());

        } catch (Exception e) {
            System.err.println("復活失敗: " + e.getMessage());
        }
    }

    public static void onRespawnComplete(ServerPlayerEntity player) {
        if (get_server("session") == 1) {
            String SQL = "SELECT pvpmode FROM playerdata WHERE UUID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, player.getUuid().toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int pvpmode = rs.getInt("pvpmode");
                        int X = randomInt(-6, 6);
                        int Z = randomInt(-6, 6);
                        int Y = (pvpmode == 0) ? 265 : 255;
                        ServerWorld world = server.getOverworld();
                        // 傳送玩家
                        TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z), Vec3d.ZERO, 0f, 0f, Entity::baseTick);
                        player.teleportTo(target);

                        player.sendMessage(Text.literal("歡迎！你已被傳送"), false);
                        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            String sql = "UPDATE playerdata SET point = point - 30 WHERE uuid = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, String.valueOf(player.getUuid()));

                int rowsAffected = stmt.executeUpdate();
                System.out.println("Database insert affected " + rowsAffected + " rows");


            } catch (SQLException e) {
                e.printStackTrace();
            }
            player.sendMessage(Text.literal("§6死亡-30分!"));
            ServerWorld world = server.getOverworld();
            int X = randomInt(-5000, 5000);
            int Z = randomInt(-5000, 5000);
            int Y = getSafeYWithValidation(world, X, Z);
            TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z), Vec3d.ZERO, 0f, 0f, Entity::baseTick);
            player.changeGameMode(GameMode.SURVIVAL);
            player.teleportTo(target);
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,  // 夜視效果
                    999999999,                       // 持續時間（ticks）：6000 = 5分鐘
                    0,                          // 等級：0 = 等級1
                    false,                      // 是否顯示粒子效果
                    false,                      // 是否顯示圖標
                    true                        // 是否來自信標（可選）
            ));
        }
        // 這裡處理復活後的邏輯
    }
    private static int getSafeYWithValidation(ServerWorld world, int x, int z) {
        try {
            // 先強制生成這個區域的地形
            forceGenerateTerrain(world, x, z);

            // 正確的 getTopY 用法：使用 BlockPos
            BlockPos checkPos = new BlockPos(x, 0, z);
            int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, checkPos);

            System.out.println("地形高度: " + surfaceY + " at " + x + ", " + z);

            // 如果高度還是太低，手動尋找安全位置
            if (surfaceY <= world.getBottomY() + 5) {
                surfaceY = findSafeYManually(world, x, z);
                System.out.println("手動找到高度: " + surfaceY);
            }

            // 最終安全檢查
            if (surfaceY <= world.getBottomY() + 5) {
                surfaceY = world.getBottomY() + 15;
                System.out.println("使用備用高度: " + surfaceY);
            }

            return surfaceY + 1; // 在表面上方向上一格

        } catch (Exception e) {
            System.err.println("獲取安全高度失敗: " + e.getMessage());
            return world.getBottomY() + 20; // 安全備用值
        }
    }

    private static void forceGenerateTerrain(ServerWorld world, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        // 強制生成以目標區塊為中心的 3x3 區域
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int targetChunkX = chunkX + dx;
                int targetChunkZ = chunkZ + dz;

                // 同步生成區塊到 FULL 狀態
                world.getChunkManager().getChunk(targetChunkX, targetChunkZ, ChunkStatus.FULL, true);
            }
        }
    }

    private static int findSafeYManually(ServerWorld world, int x, int z) {
        BlockPos checkPos = new BlockPos(x, 0, z);
        // 從世界頂部向下尋找第一個固體方塊
        for (int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, checkPos) - 10; y > world.getBottomY() + 10; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);

            if (isSolidForStanding(state)) {
                // 檢查上方是否有足夠空間
                if (hasEnoughSpace(world, x, y + 1, z)) {
                    return y + 1;
                }
            }
        }

        return world.getBottomY() + 20; // 備用
    }

    // 輔助方法
    private static boolean isSolidForStanding(BlockState state) {
        return state.blocksMovement() &&
                !state.isAir() &&
                !state.getFluidState().isEmpty() &&
                !state.isOf(Blocks.VINE) &&
                !state.isOf(Blocks.COBWEB);
    }

    private static boolean hasEnoughSpace(World world, int x, int y, int z) {
        BlockPos feetPos = new BlockPos(x, y, z);
        BlockPos headPos = new BlockPos(x, y + 1, z);

        return world.getBlockState(feetPos).isAir() &&
                world.getBlockState(headPos).isAir();
    }

    // Helper method for random numbers
    private static int randomInt(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
}
