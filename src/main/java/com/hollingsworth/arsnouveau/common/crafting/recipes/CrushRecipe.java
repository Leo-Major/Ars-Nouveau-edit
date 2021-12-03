package com.hollingsworth.arsnouveau.common.crafting.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.setup.RecipeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrushRecipe implements Recipe<Container> {

    public final Ingredient input;
    public final List<CrushOutput> outputs;
    public final ResourceLocation id;
    public static final String RECIPE_ID = "crush";
    public CrushRecipe(ResourceLocation id, Ingredient input, List<CrushOutput> outputs){
        this.input = input;
        this.outputs = outputs;
        this.id = id;
    }

    public List<ItemStack> getRolledOutputs(Random random){
        List<ItemStack> finalOutputs = new ArrayList<>();
        for(CrushOutput crushRoll : outputs){
            if(random.nextDouble() <= crushRoll.chance){
                finalOutputs.add(crushRoll.stack.copy());
            }
        }

        return finalOutputs;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        return this.input.test(inventory.getItem(0));
    }

    @Nonnull
    @Override
    public ItemStack assemble(Container inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.CRUSH_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(new ResourceLocation(ArsNouveau.MODID, RECIPE_ID));
    }

    public static class CrushOutput{
        public ItemStack stack;
        public float chance;

        public CrushOutput(ItemStack stack, float chance){
            this.stack = stack;
            this.chance = chance;
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CrushRecipe> {

        @Override
        public CrushRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "input"));
            JsonArray outputs = GsonHelper.getAsJsonArray(json,"output");
            List<CrushOutput> parsedOutputs = new ArrayList<>();

            for(JsonElement e : outputs){
                JsonObject obj = e.getAsJsonObject();
                float chance = GsonHelper.getAsFloat(obj, "chance");
                ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(obj, "item"));
                parsedOutputs.add(new CrushOutput(output, chance));
            }

            return new CrushRecipe(recipeId, input, parsedOutputs);
        }


        @Override
        public void toNetwork(FriendlyByteBuf buf, CrushRecipe recipe) {
            buf.writeInt(recipe.outputs.size());
            recipe.input.toNetwork(buf);
            for(CrushOutput i : recipe.outputs){
                buf.writeFloat(i.chance);
                buf.writeItemStack(i.stack, false);
            }
        }

        @Nullable
        @Override
        public CrushRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int length = buffer.readInt();
            Ingredient input = Ingredient.fromNetwork(buffer);
            List<CrushOutput> stacks = new ArrayList<>();

            for(int i = 0; i < length; i++){
                try{
                    float chance = buffer.readFloat();
                    ItemStack outStack = buffer.readItem();
                    stacks.add(new CrushOutput(outStack, chance));
                }catch (Exception e){
                    e.printStackTrace();
                    break;
                }
            }
            return new CrushRecipe(recipeId, input, stacks);
        }
    }
}
