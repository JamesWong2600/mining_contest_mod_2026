package org.link_uuid.miningcontest;

import net.fabricmc.api.ModInitializer;
import org.link_uuid.miningcontest.blockregister.diamond_re_rule.original_diamond_gen_remove;
import org.link_uuid.miningcontest.blockregister.ores;
import org.link_uuid.miningcontest.worldgen.uranium_expose.PlacementModifierTypeRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static org.link_uuid.miningcontest.items.initer.item_init;
import static org.link_uuid.miningcontest.items.item.ignots.ignot_init;
import static org.link_uuid.miningcontest.payload.payload_register.payload_register_init;
import org.link_uuid.miningcontest.payload.payload_register.*;
public class MiningContestCommon implements ModInitializer {
    public static final String MOD_ID = "mining_contest_mod_2026";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        item_init();
        payload_register_init();
        original_diamond_gen_remove.register();
        PlacementModifierTypeRegister.register();
        LOGGER.info("Mining Contest Mod Common initialized!");
        System.out.println("Mining Contest Mod Common initialized!");
    }
}
