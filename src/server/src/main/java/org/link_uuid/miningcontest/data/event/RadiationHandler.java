package org.link_uuid.miningcontest.data.event;

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.link_uuid.miningcontest.data.cache.Cache;
import org.link_uuid.miningcontest.payload.packets.RadiationPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static org.link_uuid.miningcontest.blockregister.ores.URANIUM_DEEPSLATE_ORE;
import static org.link_uuid.miningcontest.blockregister.ores.URANIUM_ORE;
import static org.link_uuid.miningcontest.items.armor.lead_armor.*;

public class RadiationHandler {


    public static final int RADIUS = 40;
    public static final int DAMAGE_RADIUS = 20;
    private static final int RADIUS_SQ = RADIUS * RADIUS;
    private static final float DAMAGE = 2.0f;
    private static final int TICKS_BETWEEN_CHECKS = 10;
    private static final Block TARGET_BLOCK = URANIUM_ORE;
    public static float nearestDistSq;
    //public static double nearestDistance = 0;
    private static float anti_radiation = 0;
    public static boolean calculated = false;
    public static double distSq;
    //public static final Identifier RADIATION_DISTANCE_PACKET =
    //      Identifier.of("mining_contest_mod_2026", "radiation_distance");


    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            //if (isNearTnt(player)) {
            ServerWorld world = (ServerWorld) player.getWorld();
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chestpalte = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack leg = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boot = player.getEquippedStack(EquipmentSlot.FEET);
            // Check if it's a golden helmet
            BlockPos playerPos = player.getBlockPos();
            int radius = RADIUS;
            double nearestDistance = Double.MAX_VALUE;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos check = playerPos.add(dx, dy, dz);
                        if (world.getBlockState(check).getBlock() == URANIUM_ORE || world.getBlockState(check).getBlock() == URANIUM_DEEPSLATE_ORE) {
                            double dist = Math.sqrt(playerPos.getSquaredDistance(check));
                            if (dist < nearestDistance) nearestDistance = dist; // 只保留最小距離
                        }
                    }
                }
            }

            // 如果沒有找到鈾礦，設定為 -1
            if (nearestDistance == Double.MAX_VALUE) {
                nearestDistance = -1;
            }

            // 更新 cache，只儲存合理範圍內的距離
            if (nearestDistance > 0 && nearestDistance <= 41) {
                Cache.put(player.getUuid(), nearestDistance);


// 建 packet 並發送

                System.out.println(nearestDistance);
                ServerPlayNetworking.send(player, new RadiationPackets(nearestDistance));


            } else {
                Cache.put(player.getUuid(), -1);
                ServerPlayNetworking.send(player, new RadiationPackets(-1));
            }


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
            if (!(anti_radiation == 1) && !(1/nearestDistance <= 0.3)){
                player.damage(world, world.getDamageSources().magic(), (1-anti_radiation) * (float) nearestDistance * 0.1f);
            }
            anti_radiation = 0;

            //}
        }
    }
}