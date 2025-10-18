package org.link_uuid.miningcontest.data.event.timer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.data.variable.variable;
import org.link_uuid.miningcontest.payload.packets.ScorePackets;
import org.link_uuid.miningcontest.payload.packets.SessionPackets;
import org.link_uuid.miningcontest.payload.packets.TimerPackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.link_uuid.miningcontest.data.cache.Cache.get;
import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class countdown {
        private static final int ONE_HOUR_TICKS = get_server("time"); // 20 ticks/sec * 60 sec/min * 60 min/hour
        private static int remainingTicks = 0;
        private static boolean isRunning = false;
        private static Runnable onCompleteCallback;

        public static void start(Runnable onComplete) {
            if (isRunning) return;


            remainingTicks = ONE_HOUR_TICKS * 20;
            isRunning = true;
            onCompleteCallback = onComplete;

            broadcastMessage("§a"+ONE_HOUR_TICKS+" timer started!");
        }

        public static void tick() {
            if (!isRunning) return;

            remainingTicks--;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                String SQL = "SELECT point FROM playerdata WHERE UUID = ?";

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(SQL)) {

                    stmt.setString(1, player.getUuid().toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int point = rs.getInt("point");
                            ServerPlayNetworking.send(player, new ScorePackets(point));
                    }
                }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            ServerPlayNetworking.send(player, new TimerPackets(getTimeRemaining()));
            ServerPlayNetworking.send(player, new SessionPackets(get_server("session")));

            }
            System.out.println(getTimeRemaining());

            // Send progress messages
            if (remainingTicks == ONE_HOUR_TICKS / 2) {
                broadcastMessage("§630 minutes remaining!");
            } else if (remainingTicks == 20 * 60 * 15) { // 15 minutes
                broadcastMessage("§615 minutes remaining!");
            } else if (remainingTicks == 20 * 60 * 5) { // 5 minutes
                broadcastMessage("§c5 minutes remaining!");
            } else if (remainingTicks == 20 * 60) { // 1 minute
                broadcastMessage("§c1 minute remaining!");
            } else if (remainingTicks <= 20 * 10 && remainingTicks % (20 * 5) == 0) { // Last 10 seconds, every 5 seconds
                broadcastMessage("§c" + (remainingTicks / 20) + " seconds remaining!");
            }

            if (remainingTicks <= 0) {
                isRunning = false;
                broadcastMessage("§6Timer finished!");
                if (onCompleteCallback != null) {
                    onCompleteCallback.run();
                }
            }
        }

        public static void stop() {
            isRunning = false;
            broadcastMessage("§cTimer stopped!");
        }

        public static boolean isRunning() {
            return isRunning;
        }

        public static int getTimeRemaining() {
            int seconds = remainingTicks / 20;
            int minutes = seconds / 60;
            int hours = minutes / 60;
            return seconds;
        }

        private static void broadcastMessage(String message) {
            if (server != null) {
                server.getPlayerManager().broadcast(Text.literal(message), false);
            }
        }

}
