package org.link_uuid.miningContestMod2026.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerAmountPackets(int playeramount) implements CustomPayload {
    public static final Identifier PLAYER_AMOUNT_PACKET =
            Identifier.of("mining-contest-mod-2026", "playeramount");
    public static final Id<PlayerAmountPackets> ID = new Id<>(PLAYER_AMOUNT_PACKET);

    public static final PacketCodec<RegistryByteBuf, PlayerAmountPackets> CODEC =
            PacketCodec.of(PlayerAmountPackets::encode, PlayerAmountPackets::decode);

    private static void encode(PlayerAmountPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.playeramount);
    }

    private static PlayerAmountPackets decode(RegistryByteBuf buf) {
        return new PlayerAmountPackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}