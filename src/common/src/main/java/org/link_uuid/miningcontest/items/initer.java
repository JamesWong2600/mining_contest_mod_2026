package org.link_uuid.miningcontest.items;

import org.link_uuid.miningcontest.blockregister.ores;

import static org.link_uuid.miningcontest.items.armor.lead_armor.lead_equip_init;
import static org.link_uuid.miningcontest.items.fuel.uranium_fuel.fuel_register;
import static org.link_uuid.miningcontest.items.item.food.iodine_power_init;
import static org.link_uuid.miningcontest.items.item.ignots.ignot_init;
import static org.link_uuid.miningcontest.items.item.ores.ore_init;
import static org.link_uuid.miningcontest.items.item.ores.uranium;
import static org.link_uuid.miningcontest.items.tool.tools.pickaxe_init;
import static org.link_uuid.miningcontest.worldgen.ores_gen.ore_gen_init;


public class initer {
    public static void item_init() {
        lead_equip_init();
        ore_init();
        ore_gen_init();
        pickaxe_init();
        iodine_power_init();
        ignot_init();
        ores.registerToItemGroups();
        fuel_register();
    }
}
