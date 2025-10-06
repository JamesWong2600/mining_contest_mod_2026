package org.link_uuid.miningContestMod2026.server_init;

import net.minecraft.server.MinecraftServer;

public class server_init {
    public static MinecraftServer server;

    public static void setServer(MinecraftServer server) {
        server_init.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void clearServer() {
        server = null;
    }
}
