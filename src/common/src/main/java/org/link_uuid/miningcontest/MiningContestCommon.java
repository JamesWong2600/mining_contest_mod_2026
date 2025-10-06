package org.link_uuid.miningcontest;

import net.fabricmc.api.ModInitializer;
import org.link_uuid.miningcontest.blockregister.ores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static org.link_uuid.miningcontest.items.initer.item_init;
import static org.link_uuid.miningcontest.payload.payload_register.payload_register_init;
import static org.link_uuid.miningcontest.worldgen.ores_gen.ore_init;
import org.link_uuid.miningcontest.payload.payload_register.*;
public class MiningContestCommon implements ModInitializer {
    public static final String MOD_ID = "mining_contest_mod_2026";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ores.registerToItemGroups();
        ore_init();
        item_init();
        item_init();
        payload_register_init();
        LOGGER.info("Mining Contest Mod Common initialized!");
        System.out.println("Mining Contest Mod Common initialized!");
    }
}
