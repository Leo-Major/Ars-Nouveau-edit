package com.hollingsworth.arsnouveau.api.spell;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.sound.ConfiguredSpellSound;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellCaster implements ISpellCaster{

    private Map<Integer, Spell> spells = new HashMap<>();
    private int slot;
    public ItemStack stack = ItemStack.EMPTY;
    public String flavorText = "";

    public SpellCaster(ItemStack stack){
        this(stack.getOrCreateTag());
        this.stack = stack;
    }

    @Nonnull
    @Override
    public Spell getSpell() {
        return spells.getOrDefault(getCurrentSlot(), new Spell());
    }

    @Override
    public @Nonnull Spell getSpell(int slot) {
        return spells.getOrDefault(slot, new Spell());
    }

    @Override
    public int getMaxSlots() {
        return 1;
    }

    @Override
    public int getCurrentSlot() {
        return slot;
    }

    @Override
    public void setCurrentSlot(int slot) {
        this.slot = slot;
        writeItem(stack);
    }

    @Override
    public void setSpell(Spell spell, int slot) {
        this.spells.put(slot, spell);
        writeItem(stack);
    }

    @Override
    public void setSpell(Spell spell) {
        this.spells.put(getCurrentSlot(), spell);
        writeItem(stack);
    }

    @Override
    public void setSpellRecipe(List<AbstractSpellPart> spellRecipe, int slot) {
        if(spells.containsKey(slot)){
            spells.get(slot).setRecipe(spellRecipe);
        }else{
            spells.put(slot, new Spell(spellRecipe));
        }
        writeItem(stack);
    }

    @NotNull
    @Override
    public ParticleColor.IntWrapper getColor(int slot) {
        return this.getSpell(slot).color.toWrapper();
    }

    @Override
    public void setFlavorText(String str) {
        this.flavorText = str;
        writeItem(stack);
    }

    @Override
    public String getSpellName(int slot) {
        return this.getSpell(slot).name;
    }

    @Override
    public String getSpellName() {
        return this.getSpellName(getCurrentSlot());
    }

    @Override
    public void setSpellName(String name) {
        this.getSpell().name = name;
        writeItem(stack);
    }

    @Override
    public void setSpellName(String name, int slot) {
        this.getSpell(slot).name = name;
        writeItem(stack);
    }

    @Override
    public String getFlavorText() {
        return flavorText == null ? "" : flavorText;
    }

    @Override
    public void setColor(ParticleColor.IntWrapper color) {
        this.getSpell().color = color.toParticleColor();
        writeItem(stack);
    }

    @Override
    public void setColor(ParticleColor.IntWrapper color, int slot) {
        this.getSpell(slot).color = color.toParticleColor();
        writeItem(stack);
    }

    @Nonnull
    @Override
    public ConfiguredSpellSound getSound(int slot) {
        return this.getSpell(slot).sound;
    }

    @Override
    public void setSound(ConfiguredSpellSound sound, int slot) {
        this.getSpell(slot).sound = sound;
        writeItem(stack);
    }

    @Nonnull
    @Override
    public ParticleColor.IntWrapper getColor() {
        return this.getSpell().color.toWrapper();
    }

    @Override
    public Map<Integer, Spell> getSpells() {
        return spells;
    }

    public CompoundTag writeTag(CompoundTag tag){
        tag.putInt("current_slot", getCurrentSlot());
        tag.putString("flavor", getFlavorText());
        CompoundTag spellTag = new CompoundTag();

        for(int i = 0; i < getMaxSlots(); i++){
            Spell spell = getSpell(i);
            spellTag.put("spell" + i, spell.serialize());
        }
        tag.put("spells", spellTag);
        tag.putInt("spell_count", getSpells().size());
        return tag;
    }

    public SpellCaster(CompoundTag itemTag){
        CompoundTag tag = itemTag.getCompound(getTagID().toString());

        this.slot = tag.contains("current_slot") ? tag.getInt("current_slot") : 1;
        this.flavorText = tag.getString("flavor");
        CompoundTag spellTag = tag.getCompound("spells");
        int spellCount = tag.getInt("spell_count");
        for(int i = 0; i < spellCount; i++){
            Spell spell = Spell.fromTag(spellTag.getCompound("spell" + i));
            this.spells.put(i, spell);
        }
    }

    public void writeItem(ItemStack stack){
        if(stack == null || stack.isEmpty()){
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag casterTag = new CompoundTag(); // Nest our tags so we dont cause conflicts
        writeTag(casterTag);
        tag.put(getTagID().toString(), casterTag);
        stack.setTag(tag);
    }

    /**
     * Writes this compound data to the provided tag, stored with the caster ID.
     * @param tag The tag to add this serialized tag to.
     */
    public void serializeOnTag(CompoundTag tag){
        CompoundTag thisData = writeTag(new CompoundTag());
        tag.put(getTagID().toString(), thisData);
    }

    @Override
    public ResourceLocation getTagID() {
        return new ResourceLocation(ArsNouveau.MODID, "caster");
    }
}
