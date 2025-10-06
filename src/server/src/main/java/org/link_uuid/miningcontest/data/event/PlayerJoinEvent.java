package org.link_uuid.miningcontest.data.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.link_uuid.miningcontest.data.config.json_init;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN;
import static org.link_uuid.miningcontest.MiningContestServer.randomInt;

public class PlayerJoinEvent {

        public static void register() {
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                // 玩家加入伺服器時執行
                server.execute(() -> {
                    // 在主執行緒中安全執行
                    onPlayerJoin(handler.getPlayer(), server);
                });
            });
        }

        private static void onPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
            // 在這裡處理玩家加入邏輯
            String playerName = player.getGameProfile().getName();
            UUID uuid = player.getGameProfile().getId();
            System.out.println("玩家 " + playerName + " 加入了遊戲");
            ServerWorld world = server.getWorld(World.OVERWORLD);
            Set<PositionFlag> flags = Set.of(
                    PositionFlag.X,           // 更新 X 座標
                    PositionFlag.Y,           // 更新 Y 座標
                    PositionFlag.Z,           // 更新 Z 座標
                    PositionFlag.Y_ROT,       // 更新 Y 軸旋轉（yaw）
                    PositionFlag.X_ROT        // 更新 X 軸旋轉（pitch）
            );
            int X = randomInt(-6, 6);
            int Z = randomInt(-6, 6);
            int Y = 265;
            TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z),  Vec3d.ZERO, 0f, 0f, Entity::baseTick);
            //player.teleportTo(world, X, Y ,Z, flags, 0, 0, true);
            player.changeGameMode(GameMode.ADVENTURE);
            player.teleportTo(target);
            player.sendMessage(Text.literal("歡迎！你已被傳送"), false);
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
            player.sendMessage(Text.literal("歡迎 " + playerName + " 來到伺服器！"));
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO playerdata (player, UUID, point, tp, pvppoint, server, pvpmode) VALUES ('" +
                         playerName + "', '" + uuid + "','" + 0 + "','" + 0 + "','" + 0 + "','" + json_init.config.server_ID + "','" + 0 + "')")
            ) {
                //String creditrecord = "INSERT INTO datafile VALUES ('" + name + "', '" + UUid + "','" + 0 + "','" + 0 + "','" + 0 + "','" + 0 + "')";
                stmt.executeUpdate();

            } catch (SQLException e2) {
                e2.printStackTrace();
            }


            // 其他初始化邏輯...
        }
    }