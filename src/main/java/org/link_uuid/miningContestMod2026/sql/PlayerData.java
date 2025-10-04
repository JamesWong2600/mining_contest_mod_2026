package org.link_uuid.miningContestMod2026.sql;

import net.minecraft.util.math.BlockPos;

import java.security.Timestamp;
import java.util.UUID;

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.x;

public class PlayerData {
    public final int id;
    public final String blocktype;
    public final int X;
    public final int Y;
    public final int Z;

    public PlayerData(int id, String blocktype, int X, int Y, int Z) {
        this.id = id;
        this.blocktype = blocktype;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }

    public int getId() { return id; }
    public String getBlockType() { return blocktype; }
    public int getX() { return X; }
    public int getY() { return Y; }
    public int getZ() { return Z; }

    // 獲取 BlockPos
    public BlockPos getBlockPos() {
        return new BlockPos(X, Y, Z);
    }

}
