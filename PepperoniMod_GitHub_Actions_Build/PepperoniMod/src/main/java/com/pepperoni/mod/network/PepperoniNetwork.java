
package com.pepperoni.mod.network;

import com.pepperoni.mod.PepperoniMod;
import com.pepperoni.mod.entity.PepperoniEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PepperoniNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(PepperoniMod.MODID, "main"),
                    () -> PROTOCOL,
                    PROTOCOL::equals,
                    PROTOCOL::equals
            );

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        CHANNEL.registerMessage(
                0,
                ActionPacket.class,
                ActionPacket::encode,
                ActionPacket::decode,
                ActionPacket::handle
        );
    }

    public static void sendToServer(int entityId, String action) {
        CHANNEL.sendToServer(new ActionPacket(entityId, action));
    }

    public static class ActionPacket {

        private final int entityId;
        private final String action;

        public ActionPacket(int entityId, String action) {
            this.entityId = entityId;
            this.action = action;
        }

        public static void encode(ActionPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.entityId);
            buffer.writeUtf(packet.action);
        }

        public static ActionPacket decode(FriendlyByteBuf buffer) {
            return new ActionPacket(
                    buffer.readVarInt(),
                    buffer.readUtf(32)
            );
        }

        public static void handle(
                ActionPacket packet,
                Supplier<NetworkEvent.Context> supplier
        ) {
            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) return;

                Entity entity = player.level().getEntity(packet.entityId);

                if (!(entity instanceof PepperoniEntity pepperoni)) return;

                if (!pepperoni.isAlive()
                        || !pepperoni.isTame()
                        || !pepperoni.isOwnedBy(player)) {
                    return;
                }

                switch (packet.action) {

                    case "talk":
                        pepperoni.talkTo(player);
                        break;

                    case "gift":
                        pepperoni.acceptGift(player);
                        break;

                    case "follow":
                        pepperoni.setCompanionMode(
                                PepperoniEntity.MODE_FOLLOW
                        );
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "Pepperoni is following you! ♥"
                                ), true
                        );
                        break;

                    case "mine":
                        pepperoni.setCompanionMode(
                                PepperoniEntity.MODE_MINE
                        );
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "Pepperoni is looking for ores! ♥"
                                ), true
                        );
                        break;

                    case "fight":
                        pepperoni.setCompanionMode(
                                PepperoniEntity.MODE_FIGHT
                        );
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "Pepperoni is now guarding you! ♥"
                                ), true
                        );
                        break;

                    case "sleep":
                        pepperoni.requestBedtime(player);
                        break;
                }
            });

            context.setPacketHandled(true);
        }
    }
}
