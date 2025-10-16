package org.link_uuid.miningcontest.event;

public class AutoRespawnConfig {
    public static boolean ENABLED = true;
    public static int DELAY_TICKS = 40; // 2秒後自動復活 (20 ticks = 1秒)

    public static void toggleAutoRespawn() {
        ENABLED = !ENABLED;
        saveConfig();
    }

    public static void setDelay(int seconds) {
        DELAY_TICKS = seconds * 20;
        saveConfig();
    }

    private static void saveConfig() {
        // 可以存儲到檔案
        try {
            // 這裡實現配置保存邏輯
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        // 從檔案載入配置
        try {
            // 這裡實現配置載入邏輯
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
