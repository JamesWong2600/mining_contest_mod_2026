package org.link_uuid.miningcontest.items.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.link_uuid.miningcontest.MiningContestCommon;

import java.util.function.Function;

public class food {
    public static final FoodComponent iodine_power = new FoodComponent.Builder().nutrition(0).saturationModifier(0f).alwaysEdible().build();


    public static Item iodine_power_register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestCommon.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static final Item iodine_power_item = iodine_power_register("iodine_powder", Item::new, new Item.Settings().food(iodine_power));


    public static void iodine_power_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(food.iodine_power_item));
    }
}
