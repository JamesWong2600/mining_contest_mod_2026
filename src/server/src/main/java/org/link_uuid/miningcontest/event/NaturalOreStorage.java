package org.link_uuid.miningcontest.event;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class NaturalOreStorage {
    private static final Set<BlockPos> naturalOres = Collections.newSetFromMap(new WeakHashMap<>());

    public static void addNaturalOre(BlockPos pos) {
        naturalOres.add(pos.toImmutable());
    }

    public static void removeNaturalOre(BlockPos pos) {
        naturalOres.remove(pos);
    }

    public static boolean isNaturalOre(BlockPos pos) {
        return naturalOres.contains(pos);
    }

    public static void clear() {
        naturalOres.clear();
    }
}