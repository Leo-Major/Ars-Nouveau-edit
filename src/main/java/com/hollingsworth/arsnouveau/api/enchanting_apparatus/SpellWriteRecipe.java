package com.hollingsworth.arsnouveau.api.enchanting_apparatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.util.CasterUtil;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.enchantment.EnchantmentRegistry;
import com.hollingsworth.arsnouveau.common.spell.casters.ReactiveCaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;
import java.util.List;

import static com.hollingsworth.arsnouveau.api.enchanting_apparatus.ReactiveEnchantmentRecipe.getParchment;

public class SpellWriteRecipe extends EnchantingApparatusRecipe{
    public SpellWriteRecipe(List<Ingredient> pedestalItems){
        this.pedestalItems = pedestalItems;
        this.id = new ResourceLocation(ArsNouveau.MODID, "spell_write");
    }
    public static final String RECIPE_ID = "spell_write";

    @Override
    public boolean isMatch(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile, @Nullable Player player) {
        int level = EnchantmentHelper.getEnchantments(reagent).getOrDefault(EnchantmentRegistry.REACTIVE_ENCHANTMENT, 0);
        ItemStack parchment = getParchment(pedestalItems);
        return !parchment.isEmpty() && !CasterUtil.getCaster(parchment).getSpell().isEmpty() && level > 0 && super.isMatch(pedestalItems, reagent, enchantingApparatusTile, player);
    }

    @Override
    public boolean doesReagentMatch(ItemStack reag) {
        return true;
    }

    @Override
    public ItemStack getResult(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile) {
        ItemStack parchment = getParchment(pedestalItems);
        ISpellCaster caster = CasterUtil.getCaster(parchment);
        ReactiveCaster reactiveCaster = new ReactiveCaster(reagent);
        reactiveCaster.setSpell(caster.getSpell());
        return reagent.copy();
    }

    @Override
    public JsonElement asRecipe() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", "ars_nouveau:" + RECIPE_ID);
        jsonobject.addProperty("sourceCost", getSourceCost());
        JsonArray pedestalArr = new JsonArray();
        for(Ingredient i : this.pedestalItems){
            JsonObject object = new JsonObject();
            object.add("item", i.toJson());
            pedestalArr.add(object);
        }
        jsonobject.add("pedestalItems", pedestalArr);
        return jsonobject;
    }

    @Override
    public RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(new ResourceLocation(ArsNouveau.MODID, RECIPE_ID));
    }
}
