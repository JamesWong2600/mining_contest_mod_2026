package org.link_uuid.miningContestMod2026.items.element_pickaxe;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.link_uuid.miningContestMod2026.MiningContestMod2026;

import java.util.function.Function;

import static net.minecraft.item.Items.register;
import static org.link_uuid.miningContestMod2026.MiningContestMod2026.LOGGER;

/* public class ModItems {
           public static final Item ELEMENT_PICKAXE = registerItem("element_pickaxe", new Item(new Item.Settings()));

            private static Item registerItem(String name, Item item) {
                return Registry.register(Registries.ITEM, Identifier.of(MiningContestMod2026.MOD_ID, name), item);
            }

            public static void registerModItems() {
                LOGGER.info(MiningContestMod2026.MOD_ID);
                ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
                    entries.add(ELEMENT_PICKAXE);
                });
            }
}
*/
public class ModItems {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestMod2026.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }
    public static final ToolMaterial GUIDITE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            500,
            5F,
            1F,
            22,
            ToolMaterial.DIAMOND.repairItems()
    );

    public static final Item element_pickaxe = register("element_pickaxe", Item::new, new Item.Settings().pickaxe(GUIDITE_TOOL_MATERIAL, 5f, 1f));

    public static void pickaxe_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.element_pickaxe));
    }

    public static Item uranium_register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestMod2026.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static final Item uranium = uranium_register("uranium", Item::new, new Item.Settings());

    public static void uranium_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.uranium));
    }

    public static Item lead_register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestMod2026.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static final Item lead = lead_register("lead", Item::new, new Item.Settings());

    public static void lead_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.lead));
    }

}


/* public class ModItems {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create identifier
        Identifier id = Identifier.of(MiningContestMod2026.MOD_ID, name);

        // Create and register item
        Item item = itemFactory.apply(settings);
        return Registry.register(Registries.ITEM, id, item);
    }

    // Simple registration without the complex key setup
    public static final Item ELEMENT_PICKAXE = register("element_pickaxe",
            Item::new,
            new Item.Settings());

    public static void initer() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(entries -> entries.add(ELEMENT_PICKAXE));
    }
}
*/