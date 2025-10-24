package org.link_uuid.miningcontest.data.config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.link_uuid.miningcontest.MiningContestCommon.MOD_ID;

public class json_init {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID+".json");

    public static Config config = new Config();

    public static class Config {
        public boolean redisEnabled = true;
        public String redisHost = "localhost";
        public int redisPort = 6379;
        public String redisPassword = "";
        public int redisTimeout = 2000;
        public int updateInterval = 20;
        public boolean debugMode = false;
        public int server_index = 1;
        public String MysqlHost = "192.168.1.118";
        public int MysqlPort = 3306;
        public String MysqlDB = "playerdata";
        public String MysqlUser = "james";
        public String MysqlPassword = "EPS2367ios2024";
        public String server_ID = "1";
        public int map_radius = 5000;
        public int time = 3600;
        public int web_server_port = 0;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String content = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(content, Config.class);
            } else {
                save();
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            save(); // 尝试创建默认配置
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}