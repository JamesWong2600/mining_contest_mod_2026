package org.link_uuid.miningContestMod2026.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PingPackets(int ping) implements CustomPayload {
    public static final Identifier PING_PACKET =
            Identifier.of("mining-contest-mod-2026", "ping");
    public static final Id<PingPackets> ID = new Id<>(PING_PACKET);

    public static final PacketCodec<RegistryByteBuf, PingPackets> CODEC =
            PacketCodec.of(PingPackets::encode, PingPackets::decode);

    private static void encode(PingPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.ping);
    }

    private static PingPackets decode(RegistryByteBuf buf) {
        return new PingPackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}