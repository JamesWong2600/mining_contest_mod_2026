package org.link_uuid.miningcontest.data.sqlite.lobby;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.*;
import net.minecraft.block.enums.StairShape;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.util.CaveSurface;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.link_uuid.miningcontest.MiningContestCommon.MOD_ID;
import static org.link_uuid.miningcontest.server_init.server_init.server;

public class set_lobby {
    public static List<blockdata> playerList = new ArrayList<>();

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
                String directionStr = rs.getString("direction");
                String slabType = rs.getString("slab_y");
                String stairFacing = rs.getString("stair_facing");
                String stairHalf = rs.getString("stair_half");
                String stairShape = rs.getString("stair_shape");
                String trapdoorIsOpen = rs.getString("trapdoor_isopen");
                String trapdoorIsPowered = rs.getString("trapdoor_ispowered");
                String trapdoorFacing = rs.getString("trapdoor_facing");
                String trapdoorHalf = rs.getString("trapdoor_half");
                // 添加到列表
                //playerList.add(new PlayerData(id, blockType, x, y, z));
                //System.out.println(id);
                // 使用讀取到的數據設置方塊
                Block block = getBlockFromString(blockType.toLowerCase()); // 使用數據庫中的 blocktype
                BlockPos pos = new BlockPos(x, y, z); // 使用數據庫中的 X, Y, Z
                ServerWorld world = server.getWorld(World.OVERWORLD);

                BlockState blockState = block.getDefaultState();

                if (directionStr != null && !directionStr.isEmpty()) {
                    Direction direction = parseDirection(directionStr);
                    if (direction != null) {
                        if (blockState.contains(Properties.HORIZONTAL_FACING) && direction.getAxis().isHorizontal()) {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, direction);
                        } else if (blockState.contains(Properties.FACING)) {
                            blockState = blockState.with(Properties.FACING, direction);
                        }
                    }
                }

                // 自动设置 slab 类型（如果是 slab）
                if (slabType != null && !slabType.isEmpty() && blockState.contains(Properties.SLAB_TYPE)) {
                    SlabType slab = parseSlabType(slabType);
                    blockState = blockState.with(Properties.SLAB_TYPE, slab);
                }

                if (blockState.contains(Properties.HORIZONTAL_FACING) &&
                        blockState.contains(Properties.BLOCK_HALF) &&
                        blockState.contains(Properties.STAIR_SHAPE)) {

                    // 設置 stair facing
                    if (stairFacing != null && !stairFacing.isEmpty()) {
                        Direction facing = parseDirection(stairFacing);
                        if (facing != null && facing.getAxis().isHorizontal()) {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, facing);
                        }
                    }

                    // 設置 stair half
                    if (stairHalf != null && !stairHalf.isEmpty() && blockState.contains(Properties.BLOCK_HALF)) {
                        String halfValue = stairHalf.toLowerCase();
                        EnumProperty<net.minecraft.block.enums.BlockHalf> halfProperty = Properties.BLOCK_HALF;

                        if (halfValue.equals("top") || halfValue.equals("upper") || halfValue.equals("1")) {
                            blockState = blockState.with(halfProperty, net.minecraft.block.enums.BlockHalf.TOP);
                        } else {
                            blockState = blockState.with(halfProperty, net.minecraft.block.enums.BlockHalf.BOTTOM);
                        }
                    }

                    // 設置 stair shape
                    if (stairShape != null && !stairShape.isEmpty() && blockState.contains(Properties.STAIR_SHAPE)) {
                        String shapeValue = stairShape.toLowerCase();
                        EnumProperty<net.minecraft.block.enums.StairShape> shapeProperty = Properties.STAIR_SHAPE;

                        if (shapeValue.equals("inner_left") || shapeValue.equals("innerleft") || shapeValue.equals("1")) {
                            blockState = blockState.with(shapeProperty, net.minecraft.block.enums.StairShape.INNER_LEFT);
                        } else if (shapeValue.equals("inner_right") || shapeValue.equals("innerright") || shapeValue.equals("2")) {
                            blockState = blockState.with(shapeProperty, net.minecraft.block.enums.StairShape.INNER_RIGHT);
                        } else if (shapeValue.equals("outer_left") || shapeValue.equals("outerleft") || shapeValue.equals("3")) {
                            blockState = blockState.with(shapeProperty, net.minecraft.block.enums.StairShape.OUTER_LEFT);
                        } else if (shapeValue.equals("outer_right") || shapeValue.equals("outerright") || shapeValue.equals("4")) {
                            blockState = blockState.with(shapeProperty, net.minecraft.block.enums.StairShape.OUTER_RIGHT);
                        } else {
                            // 默認為 straight
                            blockState = blockState.with(shapeProperty, net.minecraft.block.enums.StairShape.STRAIGHT);
                        }
                    }
                }

                if (blockState.contains(Properties.HORIZONTAL_FACING) &&
                        blockState.contains(Properties.OPEN) &&
                        blockState.contains(Properties.POWERED) &&
                        blockState.contains(Properties.BLOCK_HALF)) {

                    // 設置 trapdoor facing
                    if (trapdoorFacing != null && !trapdoorFacing.isEmpty()) {
                        Direction facing = parseDirection(trapdoorFacing);
                        if (facing != null && facing.getAxis().isHorizontal()) {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, facing);
                        }
                    }

                    // 設置 trapdoor open
                    if (trapdoorIsOpen != null && !trapdoorIsOpen.isEmpty()) {
                        boolean isOpen = trapdoorIsOpen.equalsIgnoreCase("true") || trapdoorIsOpen.equals("1");
                        blockState = blockState.with(Properties.OPEN, isOpen);
                    }

                    // 設置 trapdoor powered
                    if (trapdoorIsPowered != null && !trapdoorIsPowered.isEmpty()) {
                        boolean isPowered = trapdoorIsPowered.equalsIgnoreCase("true") || trapdoorIsPowered.equals("1");
                        blockState = blockState.with(Properties.POWERED, isPowered);
                    }

                    // 設置 trapdoor half
                    if (trapdoorHalf != null && !trapdoorHalf.isEmpty() && blockState.contains(Properties.BLOCK_HALF)) {
                        String halfValue = trapdoorHalf.toLowerCase();
                        EnumProperty<net.minecraft.block.enums.BlockHalf> halfProperty = Properties.BLOCK_HALF;

                        if (halfValue.equals("top") || halfValue.equals("upper") || halfValue.equals("1")) {
                            blockState = blockState.with(halfProperty, net.minecraft.block.enums.BlockHalf.TOP);
                        } else {
                            blockState = blockState.with(halfProperty, net.minecraft.block.enums.BlockHalf.BOTTOM);
                        }
                    }
                }

                if (blockType.toLowerCase().contains("leaves")) {
                    // 設置樹葉為永久不消失
                    if (blockState.contains(Properties.PERSISTENT)) {
                        blockState = blockState.with(Properties.PERSISTENT, true);
                    }
                }

                world.setBlockState(pos, blockState);



            }

            rs.close();
            stmt.close();
            conn.close();

            //System.out.println("快速載入完成: " + playerList.size() + " 筆資料");

        } catch (Exception e) {
            System.out.println("failed " + e.getMessage());
        }
    }

    private static Direction parseDirection(String directionStr) {
        if (directionStr == null || directionStr.isEmpty()) {
            return Direction.NORTH; // 默认方向
        }

        String lower = directionStr.toLowerCase();
        switch (lower) {
            case "down": return Direction.DOWN;
            case "up": return Direction.UP;
            case "north": return Direction.NORTH;
            case "south": return Direction.SOUTH;
            case "west": return Direction.WEST;
            case "east": return Direction.EAST;
            case "0": return Direction.DOWN;
            case "1": return Direction.UP;
            case "2": return Direction.NORTH;
            case "3": return Direction.SOUTH;
            case "4": return Direction.WEST;
            case "5": return Direction.EAST;
            default: return Direction.NORTH; // 默认方向
        }
    }
    private static SlabType parseSlabType(String slabType) {
        if (slabType == null || slabType.isEmpty()) {
            return SlabType.BOTTOM; // 默认值
        }

        String lower = slabType.toLowerCase();
        switch (lower) {
            case "top":
            case "upper":
            case "1":
                return SlabType.TOP;

            case "double":
            case "full":
            case "2":
                return SlabType.DOUBLE;

            case "bottom":
            case "lower":
            case "0":
            default:
                return SlabType.BOTTOM;
        }
    }


    public static Block getBlockFromString(String blockName) {
        // 從字符串獲取方塊，例如 "minecraft:stone"
        Identifier blockId = Identifier.of(blockName);
        return Registries.BLOCK.get(blockId);
    }
}
