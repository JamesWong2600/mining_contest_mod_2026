package org.link_uuid.miningcontest.mixins;


import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class remove_silk_touch_entirely {
    @Inject(method = "getPossibleEntries", at = @At("RETURN"), cancellable = true)
    private static void removeSilkTouch(
            int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {

        List<EnchantmentLevelEntry> entries = cir.getReturnValue();
        if (entries != null) {
            entries.removeIf(entry -> entry.enchantment().matchesKey(Enchantments.SILK_TOUCH));
            cir.setReturnValue(entries);
        }
    }
}
