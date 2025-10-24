package org.link_uuid.miningcontest.server_init;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

public class set_boarder {
    public static void setWorldBorder(ServerWorld world, int radius) {
        WorldBorder border = world.getWorldBorder();

        // Set center at world spawn (or specify custom coordinates)
        BlockPos spawnPos = world.getSpawnPos();
        border.setCenter(spawnPos.getX(), spawnPos.getZ());

        // Set size (radius * 2 = diameter)
        border.setSize(radius * 2);

        System.out.println("World border set to radius: " + radius);
    }

}
