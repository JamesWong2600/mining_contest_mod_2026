package org.link_uuid.miningcontest.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.link_uuid.miningcontest.data.mysqlserver.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import static org.link_uuid.miningcontest.blockregister.ores.*;

public class BlockBreakGetScore {

    public static void init() {
        // 初始化自然生成標記系統
        NaturalOreMarker.init();

        // 註冊方塊破壞事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient()) {
                handleOreMining(player, pos, state);
            }
        });
    }

    public static void handleOreMining(PlayerEntity player, BlockPos pos, BlockState state) {
        // 檢查是否是自然生成的礦物
        if (!NaturalOreMarker.isNaturalOre(pos)) {
            player.sendMessage(Text.literal("§7玩家放置礦物不計分"), true);
            return;
        }

        // 移除自然標記（防止重複計算）
        NaturalOreStorage.removeNaturalOre(pos);

        // 原有的分數邏輯
        if (state.getBlock().equals(URANIUM_ORE) || state.getBlock().equals(URANIUM_DEEPSLATE_ORE)) {
            giveOreReward(player, pos, "鈾原礦", 500, 600);
        }
        // 處理鉛礦
        else if (state.getBlock().equals(LEAD_ORE) || state.getBlock().equals(LEAD_DEEPSLATE_ORE)) {
            giveOreReward(player, pos, "鉛原礦", 15, 20);
        }
        // 處理碘礦
        else if (state.getBlock().equals(IODINE_ORE) || state.getBlock().equals(IODINE_DEEPSLATE_ORE)) {
            giveOreReward(player, pos, "碘原礦", 10, 15);
        }
        else if (state.getBlock().equals(DIAMOND_ORE_LEVEL_ONE)) {
            giveOreReward(player, pos, "鑽石原礦I", 5, 10);
        }
        else if (state.getBlock().equals(DIAMOND_ORE_LEVEL_TWO) || state.getBlock().equals(DEEPSLATE_DIAMOND_ORE_LEVEL_TWO)) {
            giveOreReward(player, pos, "鑽石原礦II", 20, 35);
        }
        else if (state.getBlock().equals(DEEPSLATE_DIAMOND_ORE_LEVEL_THREE)) {
            giveOreReward(player, pos, "鑽石原礦III", 70, 85);
        }
        else if (state.getBlock().equals(Blocks.GOLD_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_GOLD_ORE)) {
            giveOreReward(player, pos, "金原礦", 8, 12);
        }
        else if (state.getBlock().equals(Blocks.IRON_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_IRON_ORE)) {
            giveOreReward(player, pos, "鐵原礦", 3, 5);
        }
        else if (state.getBlock().equals(Blocks.REDSTONE_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_REDSTONE_ORE)) {
            giveOreReward(player, pos, "紅石礦", 6, 10);
        }
        else if (state.getBlock().equals(Blocks.LAPIS_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_LAPIS_ORE)) {
            giveOreReward(player, pos, "青金石礦", 8, 10);
        }
        else if (state.getBlock().equals(Blocks.EMERALD_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_EMERALD_ORE)) {
            giveOreReward(player, pos, "綠寶石礦", 300, 380);
        }
        else if (state.getBlock().equals(Blocks.COAL_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_COAL_ORE)) {
            giveOreReward(player, pos, "煤礦", 1, 2);
        }
        else if (state.getBlock().equals(Blocks.COPPER_ORE) || state.getBlock().equals(Blocks.DEEPSLATE_COPPER_ORE)) {
            giveOreReward(player, pos, "銅礦", 1, 2);
        }
    }

    private static void giveOreReward(PlayerEntity player, BlockPos pos, String oreName, int minScore, int maxScore) {
        // 生成隨機分數
        int baseScore = getRandomScore(minScore, maxScore);
        boolean isCritical = isCriticalHit();

        int finalScore = baseScore;
        Formatting textColor = Formatting.GREEN;
        String message = oreName + "+" + baseScore + "分";

        if (isCritical) {
            finalScore = baseScore * 2;
            textColor = Formatting.GOLD;
            message = "✨ " + oreName + " 暴擊！+" + finalScore + "分 ✨";
            playCriticalSound(player, pos);
            spawnCriticalParticles(player);
        } else {
            playSound(player, pos);
        }

        // 發送消息
        player.sendMessage(Text.literal(message).formatted(textColor), false);

        // 增加分數到數據庫
        addScoreToDatabase(player, finalScore);
    }

    private static void addScoreToDatabase(PlayerEntity player, int score) {
        String sql = "UPDATE playerdata SET point = point + ? WHERE uuid = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, score);
            stmt.setString(2, String.valueOf(player.getUuid()));

            int rowsAffected = stmt.executeUpdate();
            System.out.println("為玩家 " + player.getName().getString() + " 添加 " + score + " 分，影響 " + rowsAffected + " 行");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 檢查是否暴擊（5% 概率）
    private static boolean isCriticalHit() {
        return ThreadLocalRandom.current().nextDouble(100) < 5.0;
    }

    // 生成隨機分數
    private static int getRandomScore(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    // 暴擊音效
    private static void playCriticalSound(PlayerEntity player, BlockPos pos) {
        player.getWorld().playSound(
                null,
                pos,
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );
    }

    // 普通音效
    private static void playSound(PlayerEntity player, BlockPos pos) {
        player.getWorld().playSound(
                null,
                pos,
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }

    // 暴擊粒子效果
    private static void spawnCriticalParticles(PlayerEntity player) {
        World world = player.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;

            serverWorld.spawnParticles(
                    ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1, player.getZ(),
                    15,
                    0.5, 0.5, 0.5,
                    0.1
            );

            serverWorld.spawnParticles(
                    ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.2
            );
        }
    }
}