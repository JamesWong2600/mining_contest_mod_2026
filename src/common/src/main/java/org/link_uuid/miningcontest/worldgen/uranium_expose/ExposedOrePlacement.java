package org.link_uuid.miningcontest.worldgen.uranium_expose;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class ExposedOrePlacement extends AbstractConditionalPlacementModifier {
    public static final Codec<ExposedOrePlacement> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("min_exposed_sides").orElse(1).forGetter(config -> config.minExposedSides)
            ).apply(instance, ExposedOrePlacement::new)
    );

    private final int minExposedSides;

    public ExposedOrePlacement(int minExposedSides) {
        this.minExposedSides = minExposedSides;
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        return hasEnoughExposedSides(context, pos);
    }

    private boolean hasEnoughExposedSides(FeaturePlacementContext context, BlockPos pos) {
        int exposedSides = 0;

        // 檢查六個方向
        for (net.minecraft.util.math.Direction direction : net.minecraft.util.math.Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);

            // 如果相鄰方塊是空氣，則這個面是暴露的
            if (context.getBlockState(neighborPos).isAir()) {
                exposedSides++;
            }
        }

        return exposedSides >= minExposedSides;
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierTypeRegister.EXPOSED_ORE_PLACEMENT;
    }
}