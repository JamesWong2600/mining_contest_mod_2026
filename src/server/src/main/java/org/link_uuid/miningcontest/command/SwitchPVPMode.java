package org.link_uuid.miningcontest.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.link_uuid.miningcontest.data.cache.Cache;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;
import org.link_uuid.miningcontest.payload.packets.PVPModePacket;
import org.link_uuid.miningcontest.payload.packets.PlayerAmountPackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.link_uuid.miningcontest.MiningContestServer.randomInt;
import static org.link_uuid.miningcontest.data.cache.Cache.getCooldown;
import static org.link_uuid.miningcontest.data.cache.Cache.setCooldown;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class SwitchPVPMode {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("pvp")
                    .executes(context -> {
                        try {
                            return executeAddAdminCommand(context);
                        } catch (Exception e) {
                            ServerCommandSource source = context.getSource();
                            source.sendMessage(Text.literal("§c命令執行時發生錯誤: " + e.getMessage()));
                            e.printStackTrace();
                            return 0;
                        }
                    })

        );
    }
    private static int executeAddAdminCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // Debug: Print what type of entity is executing the command
        System.out.println("=== Command Debug ===");
        System.out.println("Source entity: " + source.getEntity());
        if (source.getEntity() != null) {
            System.out.println("Entity type: " + source.getEntity().getType());
            System.out.println("Entity class: " + source.getEntity().getClass().getName());
        }

        // Check command block first
        if (isCommandBlock(source)) {
            source.sendMessage(Text.literal("§c不能在指令方塊執行此命令"));
            return 0;
        }

        // Check if console
        if (source.getEntity() == null) {
            System.out.println("Executing as console");
            return 0;
        }

        // Check if player
        if (source.getEntity() instanceof ServerPlayerEntity) {
            System.out.println("sdk"+String.valueOf(getCooldown(source.getEntity().getUuid(), "PVPcooldown")));
            if(!(getCooldown(source.getEntity().getUuid(), "PVPcooldown") >= 0)) {
                GetPlayerCurrentPVPMode(source.getEntity().getUuid(),server, (ServerPlayerEntity) source.getEntity());
            }else{
                source.sendMessage(Text.literal("§c距離指令冷卻結束還有"+String.valueOf(getCooldown(source.getEntity().getUuid(), "PVPcooldown"))+"秒"));
            }
            return 0;
        }

        source.sendMessage(Text.literal("§c未知的指令執行者類型"));
        return 0;
    }

    private static Void GetPlayerCurrentPVPMode(UUID Uuid, MinecraftServer server, ServerPlayerEntity player) {
        String SQL = "SELECT pvpmode FROM playerdata WHERE UUID = ?";
        String update_SQL = "UPDATE playerdata SET pvpmode = ? WHERE UUID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             PreparedStatement update_stmt = conn.prepareStatement(update_SQL)) {

            stmt.setString(1, Uuid.toString());
            System.out.println("Executing SQL query for UUID: " + Uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int pvpmode = rs.getInt("pvpmode");
                    ServerWorld world = server.getWorld(World.OVERWORLD);
                    Set<PositionFlag> flags = Set.of(
                            PositionFlag.X,           // 更新 X 座標
                            PositionFlag.Y,           // 更新 Y 座標
                            PositionFlag.Z,           // 更新 Z 座標
                            PositionFlag.Y_ROT,       // 更新 Y 軸旋轉（yaw）
                            PositionFlag.X_ROT        // 更新 X 軸旋轉（pitch）
                    );
                    int X = randomInt(-6, 6);
                    int Z  = randomInt(-6, 6);
                    int[] Y = new int[1];
                    if (pvpmode == 0){
                        setCooldown(player.getUuid(), "PVPcooldown", 120);
                        update_stmt.setInt(1,1);
                        update_stmt.setString(2,Uuid.toString());
                        ServerPlayNetworking.send(player, new PVPModePacket(1));
                        Y[0] = 255;
                        equip(player);
                    }else{
                        setCooldown(player.getUuid(), "PVPcooldown", 120);
                        update_stmt.setInt(1,0);
                        update_stmt.setString(2,Uuid.toString());
                        Y[0] = 265;
                        reset_equip(player);
                        ServerPlayNetworking.send(player, new PVPModePacket(0));
                    }
                    update_stmt.executeUpdate();
                    TeleportTarget target = new TeleportTarget(world, new Vec3d(X, Y[0], Z),  Vec3d.ZERO, 0f, 0f, Entity::baseTick);
                    player.teleportTo(target);
                    player.sendMessage(Text.literal("歡迎！你已被傳送"), false);
                    player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 20, 0);
                 }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isCommandBlock(ServerCommandSource source) {
        if (source.getEntity() == null) return false;

        // Check for different types of command blocks
        boolean isCommandBlock = source.getEntity() instanceof CommandBlockMinecartEntity;

        // For Minecraft 1.21, also check the entity type
        if (!isCommandBlock) {
            EntityType<?> entityType = source.getEntity().getType();
            isCommandBlock = entityType == EntityType.COMMAND_BLOCK_MINECART;
        }

        return isCommandBlock;
    }

    private static void equip(ServerPlayerEntity player){
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        ItemStack weapon = new ItemStack(Items.DIAMOND_SWORD);

        // 設置裝備到對應欄位
        player.equipStack(EquipmentSlot.HEAD, helmet);
        player.equipStack(EquipmentSlot.CHEST, chestplate);
        player.equipStack(EquipmentSlot.LEGS, leggings);
        player.equipStack(EquipmentSlot.FEET, boots);
        player.equipStack(EquipmentSlot.MAINHAND, weapon);
    }
    private static void reset_equip(ServerPlayerEntity player){
        player.getInventory().clear();
    }


}
