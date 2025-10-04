package org.link_uuid.miningContestMod2026.sql;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.*;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.util.math.ColumnPos.getX;
import static org.link_uuid.miningContestMod2026.MiningContestMod2026.MOD_ID;
import static org.link_uuid.miningContestMod2026.server_init.server_init.server;


public class set_lobby {
    public static List<PlayerData> playerList = new ArrayList<>();

    public static void lobbyLoad() {
        try {
            Path dbPath = FabricLoader.getInstance().getConfigDir()
                    .resolve(MOD_ID)
                    .resolve("database.db");

            String url = "jdbc:sqlite:" + dbPath;
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM blockdata ORDER BY id");

            while (rs.next()) {
                int id = rs.getInt("id");
                String blockType = rs.getString("blocktype");
                int x = rs.getInt("X");
                int y = rs.getInt("Y");
                int z = rs.getInt("Z");

                // 添加到列表
                //playerList.add(new PlayerData(id, blockType, x, y, z));
                //System.out.println(id);
                // 使用讀取到的數據設置方塊
                Block block = getBlockFromString(blockType.toLowerCase()); // 使用數據庫中的 blocktype
                BlockPos pos = new BlockPos(x, y, z); // 使用數據庫中的 X, Y, Z
                ServerWorld world = server.getWorld(World.OVERWORLD);
                world.setBlockState(pos, block.getDefaultState());

            }

            rs.close();
            stmt.close();
            conn.close();

            //System.out.println("快速載入完成: " + playerList.size() + " 筆資料");

        } catch (Exception e) {
            System.out.println("failed " + e.getMessage());
        }
    }
    public static Block getBlockFromString(String blockName) {
        // 從字符串獲取方塊，例如 "minecraft:stone"
        Identifier blockId = Identifier.of(blockName);
        return Registries.BLOCK.get(blockId);
    }
}
