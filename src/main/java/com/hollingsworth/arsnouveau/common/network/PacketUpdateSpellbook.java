package com.hollingsworth.arsnouveau.common.network;

import com.hollingsworth.arsnouveau.api.util.StackUtil;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketUpdateSpellbook{

   String spellRecipe;
   int cast_slot;
   String spellName;

    public PacketUpdateSpellbook(){}

    public PacketUpdateSpellbook(String spellRecipe, int cast_slot, String spellName){
        this.spellRecipe = spellRecipe;
        this.cast_slot = cast_slot;
        this.spellName = spellName;
    }

    //Decoder
    public PacketUpdateSpellbook(FriendlyByteBuf buf){
        spellRecipe = buf.readUtf(32767);
        cast_slot = buf.readInt();
        spellName = buf.readUtf(32767);
    }

    //Encoder
    public void toBytes(FriendlyByteBuf buf){
        buf.writeUtf(spellRecipe);
        buf.writeInt(cast_slot);
        buf.writeUtf(spellName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getSender() != null){
                ItemStack stack = StackUtil.getHeldSpellbook(ctx.get().getSender());
                if(stack != null && stack.getItem() instanceof SpellBook && spellRecipe != null){
                    CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
                    SpellBook.setRecipe(tag, spellRecipe, cast_slot);
                    SpellBook.setSpellName(tag, spellName, cast_slot);
                    SpellBook.setMode(tag, cast_slot);
                    stack.setTag(tag);
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->ctx.get().getSender()), new PacketUpdateBookGUI(tag));
                }
            }
        });
        ctx.get().setPacketHandled(true);

    }
}
