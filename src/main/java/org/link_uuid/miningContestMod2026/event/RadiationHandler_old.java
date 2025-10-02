package org.link_uuid.miningContestMod2026.event;

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static org.link_uuid.miningContestMod2026.armor.lead.lead_helmet.*;
import static org.link_uuid.miningContestMod2026.blocks.uranium.uranium_ore.uraniumn_ore;

public class RadiationHandler_old {
    public static final int RADIUS = 40;
    public static final int DAMAGE_RADIUS = 20;
    private static final int RADIUS_SQ = RADIUS * RADIUS;
    private static final float DAMAGE = 2.0f;
    private static final int TICKS_BETWEEN_CHECKS = 10;
    private static final Block TARGET_BLOCK = uraniumn_ore;
    private static int nearestDistSq;
    private static float anti_radiation = 0;
    public static boolean calculated = false;
    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isNearTnt(player)) {
                ServerWorld world = player.getWorld();
                ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                ItemStack chestpalte = player.getEquippedStack(EquipmentSlot.CHEST);
                ItemStack leg = player.getEquippedStack(EquipmentSlot.LEGS);
                ItemStack boot = player.getEquippedStack(EquipmentSlot.FEET);
                // Check if it's a golden helmet
                if (helmet.getItem() == lead_helmet_item) {
                    anti_radiation+= 0.2F;
                }
                if (chestpalte.getItem() == lead_chestplate_item) {
                    anti_radiation+= 0.4F;
                }
                if (leg.getItem() == lead_legging_item) {
                    anti_radiation+= 0.3F;
                }
                if (boot.getItem() == lead_boot_item) {
                    anti_radiation+= 0.1F;
                }
                if (!(anti_radiation == 1)){
                player.damage(world, world.getDamageSources().magic(), 1-anti_radiation);
                }
                anti_radiation = 0;

            }
        }
    }

    private static boolean isNearTnt(ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        int radius = DAMAGE_RADIUS;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos check = playerPos.add(dx, dy, dz);
                    if (player.getWorld().getBlockState(check).getBlock() == uraniumn_ore) {
                        double distSq = playerPos.getSquaredDistance(check);
                        if (distSq <= radius * radius) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Called from your ModInitializer to hook into player ticks */
   /*  public static void register() {

        ServerTickEvents.START_WORLD_TICK.register((ServerWorld world) -> {
            // iterate players in this world (server-side)
            for (ServerPlayerEntity player : world.getPlayers()) {
                // throttle frequency to reduce CPU
                if (player.age % TICKS_BETWEEN_CHECKS != 0) continue;

                BlockPos playerPos = player.getBlockPos();
                int r = RADIUS;
                for (int dx = -r; dx <= r; dx++) {
                    int xx = playerPos.getX() + dx;
                    for (int dy = -r; dy <= r; dy++) {
                        int yy = playerPos.getY() + dy;
                        for (int dz = -r; dz <= r; dz++) {
                            int zz = playerPos.getZ() + dz;

                            // spherical radius check
                            nearestDistSq = dx*dx + dy*dy + dz*dz;
                            if (nearestDistSq > RADIUS_SQ) continue;

                            BlockPos checkPos = new BlockPos(xx, yy, zz);
                            if (world.getBlockState(checkPos).getBlock() == TARGET_BLOCK) {
                                // use the server-world-aware damage method
                                player.damage(world, world.getDamageSources().magic(), 3);
                                // stop scanning further positions for this player this tick
                                //dx = r + 1; dy = r + 1; dz = r + 1; // break all loops (simple)

                            }
                        }
                    }
                }
            }
        });

    }*/
}
