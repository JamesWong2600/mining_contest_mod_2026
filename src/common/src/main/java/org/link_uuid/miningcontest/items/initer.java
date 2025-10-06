package org.link_uuid.miningcontest.items;

import static org.link_uuid.miningcontest.items.armor.lead_armor.lead_equip_init;
import static org.link_uuid.miningcontest.items.item.ores.ore_init;
import static org.link_uuid.miningcontest.items.tool.tools.pickaxe_init;


public class initer {
    public static void item_init() {
        lead_equip_init();
        ore_init();
        pickaxe_init();
    }
}
