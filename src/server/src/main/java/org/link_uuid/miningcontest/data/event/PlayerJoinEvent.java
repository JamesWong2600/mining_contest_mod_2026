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
import java.sql.ResultSet;
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
        String playerName = player.getGameProfile().getName();
        UUID uuid = player.getGameProfile().getId();
        System.out.println("玩家 " + playerName + " 加入了遊戲");

        // 先執行傳送邏輯
        ServerWorld world = server.getWorld(World.OVERWORLD);


        // 使用 ON DUPLICATE KEY UPDATE 處理重複
        String sql = "INSERT INTO playerdata (player, UUID, point, tp, pvppoint, server, pvpmode) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "player = VALUES(player), server = VALUES(server)";

        String sql_admin = "SELECT UUID FROM admindata WHERE UUID = ?";
        String sql_player = "SELECT UUID FROM playerdata WHERE UUID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             PreparedStatement stmt_admin = conn.prepareStatement(sql_admin);
             PreparedStatement stmt_player = conn.prepareStatement(sql_player)) {

            // 設置基本參數
            stmt.setString(1, playerName);
            stmt.setString(2, uuid.toString());
            stmt.setInt(3, 0);
            stmt.setInt(4, 0);
            stmt.setInt(5, 0);
            stmt.setInt(6, Integer.parseInt(json_init.config.server_ID));
            stmt.setInt(7, 0);

            stmt_admin.setString(1, uuid.toString());
            stmt_player.setString(1, uuid.toString());

            // 1. 先檢查是否為管理員
            boolean isAdmin = false;
            boolean isNewPlayer = false;

            try (ResultSet rs_admin = stmt_admin.executeQuery()) {
                isAdmin = rs_admin.next();
            }

            // 2. 檢查是否為新玩家
            try (ResultSet rs_player = stmt_player.executeQuery()) {
                isNewPlayer = !rs_player.next(); // 如果沒有結果，就是新玩家
            }

            // 3. 執行插入/更新操作
            int affectedRows = stmt.executeUpdate();

            System.out.println((isAdmin ? "✅ 管理員" : "❌ 普通玩家") + ": " + playerName);
            System.out.println((isNewPlayer ? "🆕 新玩家" : "🔁 回歸玩家") + ", 受影響行數: " + affectedRows);

            // 4. 根據玩家類型執行不同邏輯
            if (isAdmin) {
                // 管理員邏輯
                handleAdminPlayer(player, world, isNewPlayer);
            } else {
                // 普通玩家邏輯
                handleNormalPlayer(player, world, isNewPlayer);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("⚠️ 玩家已存在: " + playerName);
            } else {
                System.err.println("❌ 資料庫錯誤: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void handleAdminPlayer(ServerPlayerEntity player, ServerWorld world, boolean isNewPlayer) {
        int X = randomInt(-6, 6);
        int Z = randomInt(-6, 6);
        int Y = 265;

        TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z), Vec3d.ZERO, 0f, 0f, Entity::baseTick);

        player.changeGameMode(GameMode.SPECTATOR);
        player.teleportTo(target);
        player.sendMessage(Text.literal("👑 歡迎管理員！你已被傳送"), false);
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        if (isNewPlayer) {
            player.sendMessage(Text.literal("🎊 這是你的第一次登入！"), false);
            System.out.println("✅ 新管理員資料已建立: " + player.getName().getString());
        } else {
            System.out.println("🔄 管理員資料已更新: " + player.getName().getString());
        }
    }

    private static void handleNormalPlayer(ServerPlayerEntity player, ServerWorld world, boolean isNewPlayer) {
        if (isNewPlayer) {
            // 只有新玩家才傳送
            int X = randomInt(-6, 6);
            int Z = randomInt(-6, 6);
            int Y = 265;

            TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z), Vec3d.ZERO, 0f, 0f, Entity::baseTick);

            player.changeGameMode(GameMode.ADVENTURE);
            player.teleportTo(target);
            player.sendMessage(Text.literal("歡迎！你已被傳送"), false);
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage(Text.literal("歡迎 " + player.getName().getString() + " 來到伺服器！"), false);

            System.out.println("✅ 新玩家資料已建立: " + player.getName().getString());
        } else {
            // 回歸玩家不傳送
            player.sendMessage(Text.literal("👋 歡迎回來 " + player.getName().getString() + "！"), false);
            System.out.println("🔄 現有玩家資料已更新: " + player.getName().getString());
        }
    }

       /* private static void onPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
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
        }*/
    }