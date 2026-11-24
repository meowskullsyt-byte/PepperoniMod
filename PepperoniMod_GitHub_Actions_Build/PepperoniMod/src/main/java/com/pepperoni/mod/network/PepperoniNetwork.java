package com.pepperoni.mod.network;

import com.pepperoni.mod.PepperoniMod;
import com.pepperoni.mod.entity.PepperoniEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Supplier;

public final class PepperoniNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PepperoniMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered = false;

    private PepperoniNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(ActionPacket.class, 0)
                .encoder(ActionPacket::encode).decoder(ActionPacket::decode)
                .consumerMainThread(ActionPacket::handle).add();
    }

    public static void sendToServer(int entityId, String action) {
        CHANNEL.sendToServer(new ActionPacket(entityId, action));
    }

    public static class ActionPacket {
        private final int entityId;
        private final String action;
        public ActionPacket(int entityId, String action) { this.entityId = entityId; this.action = action; }
        public static void encode(ActionPacket msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId);
            buf.writeUtf(msg.action, 32);
        }
        public static ActionPacket decode(FriendlyByteBuf buf) {
            return new ActionPacket(buf.readVarInt(), buf.readUtf(32));
        }
        public static void handle(ActionPacket msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player == null) return;
                Entity found = player.level().getEntity(msg.entityId);
                if (!(found instanceof PepperoniEntity pepperoni) || !pepperoni.isOwnedBy(player)) return;
                switch (msg.action) {
                    case "talk" -> pepperoni.talkTo(player);
                    case "gift" -> pepperoni.acceptGift(player);
                    case "follow" -> pepperoni.setCompanionMode(PepperoniEntity.MODE_FOLLOW);
                    case "mine" -> pepperoni.setCompanionMode(PepperoniEntity.MODE_MINE);
                    case "fight" -> pepperoni.setCompanionMode(PepperoniEntity.MODE_FIGHT);
                    case "sleep" -> pepperoni.requestBedtime(player);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
