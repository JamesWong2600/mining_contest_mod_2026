package org.link_uuid.miningcontest;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.link_uuid.miningcontest.command.AddAdminCommand;
import org.link_uuid.miningcontest.command.StartGame;
import org.link_uuid.miningcontest.command.SwitchPVPMode;
import org.link_uuid.miningcontest.data.config.json_init;
import org.link_uuid.miningcontest.data.event.PlayerJoinEvent;
import org.link_uuid.miningcontest.data.event.RadiationHandler;
import org.link_uuid.miningcontest.data.event.timer.countdown;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.data.redis.RedisManager;
import org.link_uuid.miningcontest.data.redis.RedisService;
import org.link_uuid.miningcontest.data.redis.ServerTickHandler;
import org.link_uuid.miningcontest.data.variable.variable;
import org.link_uuid.miningcontest.event.BlockBreakGetScore;
import org.link_uuid.miningcontest.event.PlayerPvP;
import org.link_uuid.miningcontest.payload.packets.*;
import org.link_uuid.miningcontest.server_init.server_init;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.START_SERVER_TICK;
import static net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.*;
import static org.link_uuid.miningcontest.MiningContestCommon.MOD_ID;
import static org.link_uuid.miningcontest.data.cache.Cache.get_server;
import static org.link_uuid.miningcontest.data.cache.Cache.put_server;
import static org.link_uuid.miningcontest.data.ping_and_mspt.mspt.getCurrentMspt;
import static org.link_uuid.miningcontest.data.ping_and_mspt.ping.getPingSafe;
import static org.link_uuid.miningcontest.data.redis.RedisService.getServerPlayerAmount;
import static org.link_uuid.miningcontest.data.sqlite.lobby.set_lobby.lobbyLoad;
import static org.link_uuid.miningcontest.event.BlockBreakGetScore.handleOreMining;
import static org.link_uuid.miningcontest.event.PlayerDeadEvent.instantRespawn;
import static org.link_uuid.miningcontest.event.PlayerDeadEvent.onRespawnComplete;
import static org.link_uuid.miningcontest.server_init.set_boarder.setWorldBorder;

public class MiningContestServer implements DedicatedServerModInitializer {

    public static Path CONFIG_DIR;
    private variable variable = new variable();
    public static int mark = 0;



    @Override
    public void onInitializeServer() {

        PlayerJoinEvent.register();
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                System.out.println("自動復活完成: " + newPlayer.getName().getString());

                // 復活後的處理（傳送、訊息等）
                onRespawnComplete(newPlayer);
            }
        });

        //PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
        //    handleOreMining(player, pos, state);
       // });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            countdown.tick(); // Update timer every tick

        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartGame.register(dispatcher); // This registers the /start command
        });

        // Register tick event for countdown
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            StartGame.tick(); // This handles the countdown timing
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 設定全域 keepInventory 為 true
            put_server("session",1);
            put_server("player_amount",1);
            put_server("time",60);
            BlockBreakGetScore.init();
            for (ServerWorld world : server.getWorlds()) {
                setWorldBorder(world, 5000);
                world.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, server);
                world.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
            }

            System.out.println("已啟用全域 keepInventory 規則");
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (get_server("session") == 1) {
                if (entity instanceof ServerPlayerEntity player) {
                    System.out.println("玩家死亡，準備自動復活: " + player.getName().getString());

                    // 立即執行復活
                    player.getServerWorld().getServer().execute(() -> {
                        if (player.isDead()) {
                            instantRespawn(player);
                        }
                    });
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AddAdminCommand.register(dispatcher);
            SwitchPVPMode.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                DatabaseManager.initialize();
                System.out.println("Database initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        });


        RedisManager.initialize();
        json_init.load();
        if (json_init.config.redisEnabled) {
            System.out.println("Configuration loaded: " + json_init.config.redisHost);
        }
        registerServerEvents();

        CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        createConfigDirectory();

        SERVER_STARTING.register(server -> {
            System.out.println("伺服器正在啟動: " + server.getVersion());
            server_init.setServer(server);

        });


        Map<UUID, Integer> playerUpdateCounters = new HashMap<>();

        JOIN.register((handler, sender, server) -> {
            variable.player_amount = server.getCurrentPlayerCount();
            //System.out.println(player_count);
            RedisService.saveServerPlayerAmount(json_init.config.server_index,  variable.player_amount);
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerId = player.getUuid();


            String playeramount = String.valueOf(getServerPlayerAmount(json_init.config.server_index));
            System.out.println("playeramount is "+playeramount);
            // 立即发送初始数据包
            int mspt = getCurrentMspt(server);
            int ping = getPingSafe(player);

            // 正确的发送方式 - 使用 ServerPlayerEntity 而不是 ServerPlayNetworkHandler
            ServerPlayNetworking.send(player, new MsptPackets(mspt));
            ServerPlayNetworking.send(player, new PingPackets(ping));
            ServerPlayNetworking.send(player, new SessionPackets(get_server("session")));
            ServerPlayNetworking.send(player, new PlayerAmountPackets(playeramount));



            // 初始化计数器
            playerUpdateCounters.put(playerId, 0);

            System.out.println("发送初始数据包给新玩家: " + player.getGameProfile().getName());
        });

        DISCONNECT.register((handler, server) -> {
            playerUpdateCounters.remove(handler.getPlayer().getUuid());
        });

        START_SERVER_TICK.register(server -> {
            if (server.getCurrentPlayerCount() < 1) return;

            variable.player_amount = server.getCurrentPlayerCount();
            //System.out.println(player_count);
            RedisService.saveServerPlayerAmount(json_init.config.server_index,  variable.player_amount);


            int mspt = getCurrentMspt(server);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();

                // 确保玩家在计数器中
                if (!playerUpdateCounters.containsKey(playerId)) {
                    playerUpdateCounters.put(playerId, 19);
                    continue;
                }

                int playerCounter = playerUpdateCounters.get(playerId) + 1;
                playerUpdateCounters.put(playerId, playerCounter);
                String playeramount = String.valueOf(getServerPlayerAmount(json_init.config.server_index));

                if (playerCounter >= 20) {
                    // 正确的发送方式
                    ServerPlayNetworking.send(player, new MsptPackets(mspt));
                    int ping = getPingSafe(player);
                    ServerPlayNetworking.send(player, new PingPackets(ping));
                    ServerPlayNetworking.send(player, new SessionPackets(get_server("session")));
                    ServerPlayNetworking.send(player, new PlayerAmountPackets(playeramount));
                    playerUpdateCounters.put(playerId, 0);
                }
            }
        });
            /*ServerTickEvents.START_SERVER_TICK.register(server -> {
                if (server.getCurrentPlayerCount() < 1) return;
                int mspt = getCurrentMspt(server);
                update_counter = update_counter + 1;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if(update_counter == 20){
                        ServerPlayNetworking.send(player, new MsptPackets(mspt));
                        int ping = getPingSafe(player);
                        ServerPlayNetworking.send(player, new PingPackets(ping));
                        ServerPlayNetworking.send(player, new SessionPackets(1));
                        update_counter = 0;
                    }
                }
            });*/
        loadConfig();

        START_SERVER_TICK.register(new ServerTickHandler());
        END_SERVER_TICK.register(RadiationHandler::onServerTick);
        PlayerPvP.register();
        }


    private void registerServerEvents() {
        // 可選：在世界載入完成後執行（更安全）
        ServerWorldEvents.LOAD.register((server, world) -> {
            System.out.println("loaded");
            lobbyLoad();
        });
    }
    private void createConfigDirectory() {
        try {
            Files.createDirectories(CONFIG_DIR);
            System.out.println("Config directory created: " + CONFIG_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + CONFIG_DIR);
            e.printStackTrace();
        }
    }
    private void loadConfig() {
        Path configFile = CONFIG_DIR.resolve("config.json");
        // 你的設定檔載入邏輯
    }

    public static int randomInt(int min, int max) {
        // 生成 min 到 max 之間的隨機整數（包含 min 和 max）
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}