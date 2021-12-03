package com.hollingsworth.arsnouveau.common.network;

import com.hollingsworth.arsnouveau.api.util.StackUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketUpdateSpellColors {

   String spellRecipe;
   int castSlot;
   String spellName;
   int r;
   int g;
   int b;

    public PacketUpdateSpellColors(){}

    public PacketUpdateSpellColors(int castSlot, int r, int g, int b){
        this.castSlot = castSlot;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    //Decoder
    public PacketUpdateSpellColors(FriendlyByteBuf buf){
        castSlot = buf.readInt();
        r = buf.readInt();
        g = buf.readInt();
        b = buf.readInt();
    }

    public PacketUpdateSpellColors(int slot, double red, double green, double blue) {
        this(slot, (int)red, (int) green, (int) blue);
    }

    //Encoder
    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(castSlot);
        buf.writeInt(r);
        buf.writeInt(g);
        buf.writeInt(b);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getSender() != null){
                ItemStack stack = StackUtil.getHeldSpellbook(ctx.get().getSender());
                if(stack != null && stack.getItem() instanceof SpellBook){
                    CompoundTag tag = stack.hasTag() ? stack.getTag() : new CompoundTag();
                    SpellBook.setSpellColor(tag, new ParticleColor.IntWrapper(r, g, b), castSlot);
                    stack.setTag(tag);
                    SpellBook.setMode(tag, castSlot);
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->ctx.get().getSender()), new PacketUpdateBookGUI(tag));
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->ctx.get().getSender()),
                            new PacketOpenSpellBook(stack.getTag(), ((SpellBook) stack.getItem()).tier.ordinal(), SpellBook.getUnlockedSpellString(tag)));

                }
            }
        });
        ctx.get().setPacketHandled(true);

    }
}
