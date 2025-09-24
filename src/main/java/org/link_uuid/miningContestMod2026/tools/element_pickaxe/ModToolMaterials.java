package org.link_uuid.miningContestMod2026.tools.element_pickaxe;

import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public class define {
    public static final ToolMaterial ELEMENT = new ToolMaterial(
            1500,                       // durability
            8.0f,                       // mining speed multiplier
            3.0f,                       // attack damage
            3,                          // mining level (3 = diamond)
            22,                         // enchantability
            Ingredient.ofItems(Items.DIAMOND) // repair ingredient
    );
}
