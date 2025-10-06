package org.link_uuid.miningcontest.items.armor;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.link_uuid.miningcontest.MiningContestCommon;

import java.util.function.Function;
import java.util.Map;
import java.util.function.Function;

public class lead_armor {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MiningContestCommon.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static final int BASE_DURABILITY = 15;
    public static final RegistryKey<EquipmentAsset> GUIDITE_ARMOR_MATERIAL_KEY = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(MiningContestCommon.MOD_ID, "lead_material"));

    public static final ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET, 3,
                    EquipmentType.CHESTPLATE, 8,
                    EquipmentType.LEGGINGS, 6,
                    EquipmentType.BOOTS, 3
            ),
            5,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            ToolMaterial.DIAMOND.repairItems(),
            GUIDITE_ARMOR_MATERIAL_KEY
    );


    public static final Item lead_helmet_item = register(
            "lead_helmet",
            Item::new,
            new Item.Settings().armor(lead_armor.INSTANCE, EquipmentType.HELMET)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(lead_armor.BASE_DURABILITY))
    );

    public static final Item lead_chestplate_item = register(
            "lead_chestplate",
            Item::new,
            new Item.Settings().armor(lead_armor.INSTANCE, EquipmentType.CHESTPLATE)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(lead_armor.BASE_DURABILITY))
    );

    public static final Item lead_legging_item = register(
            "lead_legging",
            Item::new,
            new Item.Settings().armor(lead_armor.INSTANCE, EquipmentType.LEGGINGS)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(lead_armor.BASE_DURABILITY))
    );

    public static final Item lead_boot_item = register(
            "lead_boot",
            Item::new,
            new Item.Settings().armor(lead_armor.INSTANCE, EquipmentType.BOOTS)
                    .maxDamage(EquipmentType.HELMET.getMaxDamage(lead_armor.BASE_DURABILITY))
    );

    public static void lead_equip_init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(lead_armor.lead_helmet_item));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(lead_armor.lead_chestplate_item));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(lead_armor.lead_legging_item));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(lead_armor.lead_boot_item));
    }

}