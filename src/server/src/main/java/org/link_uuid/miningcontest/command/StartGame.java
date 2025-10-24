package org.link_uuid.miningcontest.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
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
import org.link_uuid.miningcontest.data.event.timer.countdown;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.data.variable.variable;
import org.link_uuid.miningcontest.payload.packets.CountdownPackets;
import org.link_uuid.miningcontest.payload.packets.MsptPackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;
import static org.link_uuid.miningcontest.data.cache.Cache.put_server;

public class StartGame {
    private static final int COUNTDOWN_SECONDS = 10;
    private static int remainingTicks = 0; // 改為 ticks
    private static boolean isCountingDown = false;
    private static Runnable onCompleteCallback;
    private static MinecraftServer server;
    private static org.link_uuid.miningcontest.data.variable.variable variable = new variable();

    // 常數：每秒的 tick 數
    private static final int TICKS_PER_SECOND = 20;

    public static void startCountdown(Runnable onComplete, MinecraftServer currentServer) {
        if (isCountingDown) {
            return;
        }

        remainingTicks = COUNTDOWN_SECONDS * TICKS_PER_SECOND; // 轉換為 ticks
        isCountingDown = true;
        onCompleteCallback = onComplete;
        server = currentServer;
    }


    public static void update_tp(){
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
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
        }
    }
    // tick() 方法修正
    public static void tick() {
        if (!isCountingDown) return;


        // 只在整秒時顯示訊息
        if (remainingTicks % TICKS_PER_SECOND == 0) {
            int secondsRemaining = remainingTicks / TICKS_PER_SECOND;

            if (secondsRemaining < 0) {
                update_tp();
                isCountingDown = false;
                broadcastMessage("§a遊戲開始!");
                if (onCompleteCallback != null) {
                    onCompleteCallback.run();
                }
            } else {
                // 在特定時間點顯示訊息
                broadcastCountdownMessage();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(player, new CountdownPackets(secondsRemaining,System.currentTimeMillis(),1));
                }
            }

        }
        remainingTicks--;
    }

    private static void broadcastCountdownMessage() {
        int secondsRemaining = remainingTicks / TICKS_PER_SECOND;
        String color = secondsRemaining <= 3 ? "§c" : "§e";
        String message = color + "遊戲將在 " + secondsRemaining + " 秒後開始...";
        broadcastMessage(message);
    }

    // 其他方法保持不變...
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("start")
                        .executes(context -> {
                            try {
                                return executeStartCommand(context);
                            } catch (Exception e) {
                                ServerCommandSource source = context.getSource();
                                source.sendMessage(Text.literal("§c命令執行時發生錯誤: " + e.getMessage()));
                                e.printStackTrace();
                                return 0;
                            }
                        })
        );
    }

    private static int executeStartCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // Check command block first
        if (isCommandBlock(source)) {
            source.sendMessage(Text.literal("§c不能在指令方塊執行此命令"));
            return 0;
        }

        // Check if console
        if (source.getEntity() == null) {
            startGameCountdown(source.getServer());
            return 1;
        }

        // Check if player
        if (source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = source.getPlayer();
            UUID admin_uuid = player.getUuid();
            return checkPlayerPermissionAndExecute(source, admin_uuid);
        }

        source.sendMessage(Text.literal("§c未知的指令執行者類型"));
        return 0;
    }

    private static int checkPlayerPermissionAndExecute(ServerCommandSource source, UUID adminUuid) {
        String SQL = "SELECT permission_level FROM admindata WHERE UUID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, adminUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int admin_permission_level = rs.getInt("permission_level");

                    if (admin_permission_level > 1) {
                        if (!isCountingDown) {
                            startGameCountdown(source.getServer());
                            return 1;
                        } else {
                            source.sendMessage(Text.literal("§c倒數計時已在進行中"));
                            return 0;
                        }
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

    private static void startGameCountdown(MinecraftServer currentServer) {
        startCountdown(() -> {
            executeGameStart();
        }, currentServer);
    }

    private static void executeGameStart() {
        System.out.println("Game starting now!");

        if (server != null) {
            // 如果你有 1 小時計時器，也應該用同樣的方式修正
            if (!countdown.isRunning()) {
                countdown.start(() -> {
                    System.out.println("Timer completed!");
                });
            }
            put_server("session",2);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if(player.interactionManager.getGameMode() == GameMode.ADVENTURE){
                UUID uuid = player.getUuid();
                String SQL = "SELECT * FROM playerdata WHERE UUID = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(SQL)) {

                    stmt.setString(1, uuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.NIGHT_VISION,  // 夜視效果
                                    999999999,                       // 持續時間（ticks）：6000 = 5分鐘
                                    0,                          // 等級：0 = 等級1
                                    false,                      // 是否顯示粒子效果
                                    false,                      // 是否顯示圖標
                                    true                        // 是否來自信標（可選）
                            ));

                            player.sendMessage(Text.literal("§6遊戲開始!"));
                            ServerWorld world = server.getOverworld();
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
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                }
            }
        }
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
    // 輔助方法：生成隨機數
    private static int randomInt(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
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

    private static boolean isCommandBlock(ServerCommandSource source) {
        if (source.getEntity() == null) return false;
        return source.getEntity() instanceof CommandBlockMinecartEntity;
    }

    public static boolean isCountdownActive() {
        return isCountingDown;
    }

    public static void cancelCountdown() {
        isCountingDown = false;
        broadcastMessage("§c倒數計時已取消!");
    }

    private static void broadcastMessage(String message) {
        if (server != null) {
            server.getPlayerManager().broadcast(Text.literal(message), false);
        }
    }
}
