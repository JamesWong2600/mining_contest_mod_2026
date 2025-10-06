package org.link_uuid.miningcontest.data.ping_and_mspt;

import net.minecraft.server.MinecraftServer;

public class mspt {
    public static int getCurrentMspt(MinecraftServer server) {
        long[] tickTimes = server.getTickTimes();
        if (tickTimes == null || tickTimes.length == 0) {
            return 0;
        }

        // Get the most recent tick time
        long recentTickTime = tickTimes[server.getTicks() % tickTimes.length];
        return (int) (recentTickTime / 1000000);
    }
}
