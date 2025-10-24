package org.link_uuid.miningcontest.data.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.link_uuid.miningcontest.data.config.json_init;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.payload.packets.CountdownPackets;
import org.link_uuid.miningcontest.payload.packets.ScorePackets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN;
import static org.link_uuid.miningcontest.MiningContestServer.randomInt;
import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.server_init.server_init.server;

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
        boolean onlineMode = server.isOnlineMode();
        UUID uuid;
        if (onlineMode) {
            // 先嘗試從外部 API 獲取 UUID
            String fetchedUUID = fetchPlayerUUID(playerName);
            if (fetchedUUID != null && !fetchedUUID.isEmpty()) {
                try {
                    uuid = UUID.fromString(fetchedUUID);
                    System.out.println("✅ 從 API 獲取 UUID: " + uuid);
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ API 返回的 UUID 格式錯誤: " + fetchedUUID);
                    uuid = player.getUuid(); // 使用玩家的 UUID 作為備用
                }
            } else {
                // API 返回 null 或空字串
                uuid = player.getUuid();
                System.out.println("⚠️ 無法從 API 獲取 UUID，使用玩家 UUID: " + uuid);
            }
        } else {
            // 離線模式直接使用玩家的 UUID
            uuid = player.getUuid();
            System.out.println("🌐 離線模式使用玩家 UUID: " + uuid);
        }

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
                if (rs_admin.next()) {
                    isAdmin = true;
                    System.out.println("管理員 UUID: " + rs_admin.getString("UUID"));
                } else {
                    System.out.println("不是管理員: " + playerName);
                }
            }

            // 2. 檢查是否為新玩家
            try (ResultSet rs_player = stmt_player.executeQuery()) {
                if (!rs_player.next()) {
                    isNewPlayer = true; // 如果沒有結果，就是新玩家
                    System.out.println("新玩家: " + playerName);
                } else {
                    System.out.println("回歸玩家 UUID: " + rs_player.getString("UUID"));
                }
            }

            System.out.println((isAdmin ? "✅ 管理員" : "❌ 普通玩家") + ": " + playerName);

            // 3. 根據玩家類型和 session 狀態執行不同邏輯
            if (isAdmin) {
                // 管理員邏輯
                handleAdminPlayer(player, world, isNewPlayer);
            } else {
                // 普通玩家邏輯
                if (get_server("session") == 2) {
                    // 檢查 tp 狀態
                    String SQL = "SELECT tp FROM playerdata WHERE UUID = ?";
                    try (Connection tp_conn = DatabaseManager.getConnection();
                         PreparedStatement tp_stmt = tp_conn.prepareStatement(SQL)) {

                        tp_stmt.setString(1, uuid.toString());

                        try (ResultSet rs = tp_stmt.executeQuery()) {
                            if (rs.next()) {
                                int tp_status = rs.getInt("tp");
                                if (tp_status == 0) {
                                    handleNonTeleportPlayerAfterStart(player, world, isNewPlayer);
                                } else {
                                    handleNormalPlayer(player, world, isNewPlayer);
                                }
                            } else {
                                // 沒有找到記錄，按正常玩家處理
                                handleNormalPlayer(player, world, isNewPlayer);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // 發生錯誤時按正常玩家處理
                        handleNormalPlayer(player, world, isNewPlayer);
                    }
                }
                else if(get_server("session") == 3){
                    handlePlayerAfterEnd(player);
                }else {
                    handleNormalPlayer(player, world, isNewPlayer);
                }
            }

            // 4. 執行資料庫插入/更新（無論哪種情況都要執行）
            int affectedRows = stmt.executeUpdate();
            System.out.println((isNewPlayer ? "🆕 新玩家" : "🔁 回歸玩家") + ", 受影響行數: " + affectedRows);

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

    private static void handlePlayerAfterEnd(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
    }


    private static void handleNonTeleportPlayerAfterStart(ServerPlayerEntity player, ServerWorld world, boolean isNewPlayer) {
        if (isNewPlayer) {
            player.networkHandler.disconnect(Text.literal("比賽已經開始"));
        } else {
            String sql = "UPDATE playerdata SET tp = ? WHERE uuid = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, 1);
                stmt.setString(2, String.valueOf(player.getUuid()));

                int rowsAffected = stmt.executeUpdate();
                System.out.println("Database insert affected " + rowsAffected + " rows");


            } catch (SQLException e) {
                e.printStackTrace();
            }
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,  // 夜視效果
                    999999999,                       // 持續時間（ticks）：6000 = 5分鐘
                    0,                          // 等級：0 = 等級1
                    false,                      // 是否顯示粒子效果
                    false,                      // 是否顯示圖標
                    true                        // 是否來自信標（可選）
            ));

            ServerPlayNetworking.send(player, new CountdownPackets(0,System.currentTimeMillis(),1));

            player.sendMessage(Text.literal("§6遊戲開始!"));
            int X = randomInt(-5000, 5000);
            int Z = randomInt(-5000, 5000);
            int Y = getSafeYWithValidation(world, X, Z);

            TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y, Z), Vec3d.ZERO, 0f, 0f, Entity::baseTick);
            player.changeGameMode(GameMode.SURVIVAL);
            player.teleportTo(target);

            ItemStack[] items = {
                    new ItemStack(Items.NETHERITE_PICKAXE),
                    new ItemStack(Items.BIRCH_BOAT),
                    new ItemStack(Items.BIRCH_DOOR, 5),
                    new ItemStack(Items.COOKED_BEEF, 8)
            };
            giveItemsToPlayer(player, items);

            player.teleportTo(target);
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            player.sendMessage(Text.literal("👋 歡迎回來 " + player.getName().getString() + "！"), false);
            System.out.println("🔄 現有玩家資料已更新: " + player.getName().getString());

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
    private static String fetchPlayerUUID(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String uuidWithoutDashes = json.get("id").getAsString();

                // Convert to proper UUID format
                return formatUUID(uuidWithoutDashes);

            } else if (responseCode == 404) {
                // Player not found
                return null;
            } else {
                // Other HTTP error
                throw new RuntimeException("HTTP " + responseCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch UUID: " + e.getMessage(), e);
        }
    }
    public static void giveItemsToPlayer(ServerPlayerEntity player, ItemStack[] items) {
        if (player == null || items == null) return;

        for (int i = 0; i < Math.min(items.length, 5); i++) {
            if (items[i] != null && !items[i].isEmpty()) {
                player.getInventory().setStack(i, items[i].copy());
            }
        }
        player.currentScreenHandler.sendContentUpdates();
    }

    private static String formatUUID(String uuidWithoutDashes) {
        // Convert from "f0b6e6a6c0a84f6e8b6a6c0a84f6e8b6" to "f0b6e6a6-c0a8-4f6e-8b6a-6c0a84f6e8b6"
        if (uuidWithoutDashes.length() != 32) {
            return uuidWithoutDashes;
        }

        return uuidWithoutDashes.substring(0, 8) + "-" +
                uuidWithoutDashes.substring(8, 12) + "-" +
                uuidWithoutDashes.substring(12, 16) + "-" +
                uuidWithoutDashes.substring(16, 20) + "-" +
                uuidWithoutDashes.substring(20, 32);
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

    }