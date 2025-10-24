package org.link_uuid.miningcontest.data.event.timer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.payload.packets.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.data.cache.Cache.put_server;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class countdown {
    private static final int ONE_HOUR_TICKS = get_server("time");
    private static int remainingTicks = 0;
    private static boolean isRunning = false;
    private static Runnable onCompleteCallback;
    private static final int TICKS_PER_SECOND = 20;
    private static int tickCounter = 0;
    // 時間追蹤
    private static long lastRealTime = 0;
    private static final long REAL_MS_PER_TICK = 50; // 50ms = 1 tick

    public static void start(Runnable onComplete) {
        if (isRunning) return;

        remainingTicks = ONE_HOUR_TICKS * 20;
        isRunning = true;
        onCompleteCallback = onComplete;
        lastRealTime = System.currentTimeMillis();

        broadcastMessage("§a"+ONE_HOUR_TICKS+" timer started!");
    }

    public static void tick() {
        if (!isRunning) return;

        // 發送玩家資料
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ServerPlayNetworking.send(player, new SessionPackets(get_server("session")));
            ServerPlayNetworking.send(player, new TimerPackets(getTimeRemaining()));
        }

        // 精確倒數邏輯
        int secondsRemaining = remainingTicks / 20;
        int currentTickInSecond = remainingTicks % 20;

        // 最後10秒的精確同步
        if (secondsRemaining <= 10 && secondsRemaining >= 0) {
            // 在每秒的第19個tick發送封包（提前發送補償延遲）
            if (currentTickInSecond == 19) {
                System.out.println("Sent countdown: " + secondsRemaining + " at tick " + remainingTicks);

                // 同時發送聊天訊息
                if (secondsRemaining > 0) {
                    broadcastMessage("§e" + secondsRemaining + " seconds remaining!");
                }
            }
        }

        // 發送提示訊息（確保只在對應的tick發送一次）
        if (remainingTicks == 30 * 60 * 20) {
            broadcastMessage("§630 minutes remaining!");
        } else if (remainingTicks == 15 * 60 * 20) {
            broadcastMessage("§615 minutes remaining!");
        } else if (remainingTicks == 5 * 60 * 20) {
            broadcastMessage("§c5 minutes remaining!");
        } else if (remainingTicks == 60 * 20) {
            broadcastMessage("§c1 minute remaining!");
        }

        // 計時器結束 - 在 remainingTicks == 0 時立即執行
        if (remainingTicks == 0) {
            isRunning = false;
            put_server("session", 3);

            // 立即發送結束封包和執行所有結束邏輯
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.changeGameMode(GameMode.SPECTATOR);
                ServerPlayNetworking.send(player, new SessionPackets(get_server("session")));
                // 發送倒數結束封包（如果有的話）
                // ServerPlayNetworking.send(player, new CountdownPackets(0, System.currentTimeMillis()));
            }

            broadcastMessage("§6Timer finished!");
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }

            // 重置 remainingTicks 避免重複執行
            remainingTicks = -1;
        }

        // 只在計時器還在運行時減少倒數
        if (isRunning) {
            remainingTicks--;
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
        return remainingTicks / 20;
    }

    private static void broadcastMessage(String message) {
        if (server != null) {
            server.getPlayerManager().broadcast(Text.literal(message), false);
        }
    }
}
