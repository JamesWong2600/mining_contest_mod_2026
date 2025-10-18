package org.link_uuid.miningcontest.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.link_uuid.miningcontest.data.config.json_init;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.*;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class AddAdminCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("addadmin")
                        .then(argument("playername", StringArgumentType.string())
                                .then(argument("permission_level", IntegerArgumentType.integer(0, 4))
                                        .executes(context -> {
                                            try {
                                                return executeAddAdminCommand(context);
                                            } catch (Exception e) {
                                                ServerCommandSource source = context.getSource();
                                                source.sendMessage(Text.literal("§c命令執行時發生錯誤: " + e.getMessage()));
                                                e.printStackTrace();
                                                return 0;
                                            }
                                        })
                                )
                        )
        );
    }
    private static int executeAddAdminCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // Debug: Print what type of entity is executing the command
        System.out.println("=== Command Debug ===");
        System.out.println("Source entity: " + source.getEntity());
        if (source.getEntity() != null) {
            System.out.println("Entity type: " + source.getEntity().getType());
            System.out.println("Entity class: " + source.getEntity().getClass().getName());
        }

        // Check command block first
        if (isCommandBlock(source)) {
            source.sendMessage(Text.literal("§c不能在指令方塊執行此命令"));
            return 0;
        }

        // Check if console
        if (source.getEntity() == null) {
            System.out.println("Executing as console");
            return addAdminToDatabase(context, source);
        }

        // Check if player
        if (source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = source.getPlayer();
            UUID admin_uuid = player.getUuid();
            System.out.println("Player UUID: " + admin_uuid);

            return checkPlayerPermissionAndExecute(context, source, admin_uuid);

        }

        source.sendMessage(Text.literal("§c未知的指令執行者類型"));
        return 0;
    }

    private static boolean isCommandBlock(ServerCommandSource source) {
        if (source.getEntity() == null) return false;

        // Check for different types of command blocks
        boolean isCommandBlock = source.getEntity() instanceof CommandBlockMinecartEntity;

        // For Minecraft 1.21, also check the entity type
        if (!isCommandBlock) {
            EntityType<?> entityType = source.getEntity().getType();
            isCommandBlock = entityType == EntityType.COMMAND_BLOCK_MINECART;
        }

        return isCommandBlock;
    }

    private static int checkPlayerPermissionAndExecute(CommandContext<ServerCommandSource> context, ServerCommandSource source, UUID adminUuid) {
        String SQL = "SELECT permission_level FROM admindata WHERE UUID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, adminUuid.toString());
            System.out.println("Executing SQL query for UUID: " + adminUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int admin_permission_level = rs.getInt("permission_level");
                    System.out.println("Found admin permission level: " + admin_permission_level);

                    if (admin_permission_level > 1) {
                        return addAdminToDatabase(context, source);
                    } else {
                        source.sendMessage(Text.literal("§c權限不足"));
                        return 0;
                    }
                } else {
                    source.sendMessage(Text.literal("§c你不在管理員名單中"));
                    return 0;
                }
            }
        } catch (SQLException e) {
            source.sendMessage(Text.literal("§c資料庫查詢錯誤"));
            e.printStackTrace();
            return 0;
        }
    }

    private static int addAdminToDatabase(CommandContext<ServerCommandSource> context, ServerCommandSource source) {
        String playerName = StringArgumentType.getString(context, "playername");
        int permissionLevel = IntegerArgumentType.getInteger(context, "permission_level");

        System.out.println("Adding admin: " + playerName + " with level: " + permissionLevel);

        // Fetch UUID
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
                    uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8)); // 使用玩家的 UUID 作為備用
                }
            } else {
                // API 返回 null 或空字串
                uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
                System.out.println("⚠️ 無法從 API 獲取 UUID，使用玩家 UUID: " + uuid);
            }
        } else {
            // 離線模式直接使用玩家的 UUID
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
            System.out.println("🌐 離線模式使用玩家 UUID: " + uuid);
        }


       //String uuid = fetchPlayerUUID(playerName);
        //if (uuid == null) {
        //    source.sendMessage(Text.literal("§c找不到玩家: " + playerName));
        //    return 0;
        //}

        // Insert into database
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO admindata (player, UUID, permission_level) VALUES (?, ?, ?)")) {

            stmt.setString(1, playerName);
            stmt.setString(2, String.valueOf(uuid));
            stmt.setInt(3, permissionLevel);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Database insert affected " + rowsAffected + " rows");

            source.sendMessage(Text.literal("§a成功添加管理員: " + playerName + " 權限等級: " + permissionLevel));
            return 1;

        } catch (SQLException e) {
            source.sendMessage(Text.literal("§c資料庫寫入失敗: " + e.getMessage()));
            e.printStackTrace();
            return 0;
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
}
