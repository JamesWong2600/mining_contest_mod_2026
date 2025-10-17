package org.link_uuid.miningcontest.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.payload.packets.PVPModePacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.link_uuid.miningcontest.MiningContestServer.randomInt;
import static org.link_uuid.miningcontest.data.cache.Cache.setCooldown;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class PlayerDeadEvent {

    public static void instantRespawn(ServerPlayerEntity deadPlayer) {
        try {
            MinecraftServer server = deadPlayer.getServer();
            ServerPlayerEntity newPlayer = server.getPlayerManager().respawnPlayer(
                    deadPlayer, false, Entity.RemovalReason.KILLED
            );

            System.out.println("已執行復活: " + newPlayer.getName().getString());

        } catch (Exception e) {
            System.err.println("復活失敗: " + e.getMessage());
        }
    }

    public static void onRespawnComplete(ServerPlayerEntity player) {
        // 這裡處理復活後的邏輯
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
    }
    // Helper method for random numbers
    private static int randomInt(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
}
