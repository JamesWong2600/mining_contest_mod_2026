package org.link_uuid.miningcontest.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class PlayerPvP {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Check if entity is a player and attacker is a player
            if (entity instanceof PlayerEntity target && source.getAttacker() instanceof PlayerEntity attacker) {
                String SQL = "SELECT pvpmode FROM playerdata WHERE UUID = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement damager_stmt = conn.prepareStatement(SQL);
                     PreparedStatement victim_stmt = conn.prepareStatement(SQL)) {

                    boolean onlineMode = server.isOnlineMode();
                    System.out.println("Attacker UUID: " + source.getAttacker().getUuid().toString());
                    System.out.println("Victim UUID: " + entity.getUuid().toString());

                    if (onlineMode) {
                        damager_stmt.setString(1, fetchPlayerUUID(source.getAttacker().getName().toString()));
                        victim_stmt.setString(1, fetchPlayerUUID(entity.getName().toString()));
                    } else {
                        damager_stmt.setString(1, source.getAttacker().getUuid().toString());
                        victim_stmt.setString(1, entity.getUuid().toString());
                    }

                    boolean damagerIsPVP = false;
                    boolean victimIsPVP = false;

                    try (ResultSet rs = damager_stmt.executeQuery()) {
                        if (rs.next()) {
                            int pvpmode = rs.getInt("pvpmode");
                            damagerIsPVP = (pvpmode == 1);
                        }
                    }
                    try (ResultSet rs = victim_stmt.executeQuery()) {
                        if (rs.next()) {
                            int pvpmode = rs.getInt("pvpmode");
                            victimIsPVP = (pvpmode == 1);
                        }
                    }

                    // Don't cancel self-damage
                    if (attacker != target) {
                        boolean shouldCancel = false;

                        if (!damagerIsPVP && victimIsPVP) {
                            attacker.sendMessage(Text.literal("§c你沒有開啓PVP模式"), false);
                            shouldCancel = true;
                        } else if (damagerIsPVP && !victimIsPVP) {
                            attacker.sendMessage(Text.literal("§c對方沒有開啓PVP模式"), false);
                            shouldCancel = true;
                        } else if (!victimIsPVP && !damagerIsPVP) {
                            attacker.sendMessage(Text.literal("§c你們兩個沒有開啓PVP模式"), false);
                            shouldCancel = true;
                        } else if (get_server("session") == 2) {
                            attacker.sendMessage(Text.literal("§c比賽進行期間無法PVP"), false);
                            shouldCancel = true;
                        }

                        // Debug output
                        System.out.println("Damager PVP: " + damagerIsPVP + ", Victim PVP: " + victimIsPVP);
                        System.out.println("Should cancel damage: " + shouldCancel);

                        return !shouldCancel; // Return true to allow damage, false to cancel
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return true; // Allow damage
        });
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
