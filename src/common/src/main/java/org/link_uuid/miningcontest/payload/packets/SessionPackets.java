package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SessionPackets(int session) implements CustomPayload {
    public static final Identifier SESSION_PACKET =
            Identifier.of("mining_contest_mod_2026", "session");
    public static final Id<SessionPackets> ID = new Id<>(SESSION_PACKET);

    public static final PacketCodec<RegistryByteBuf, SessionPackets> CODEC =
            PacketCodec.of(SessionPackets::encode, SessionPackets::decode);

    private static void encode(SessionPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.session);
    }

    private static SessionPackets decode(RegistryByteBuf buf) {
        return new SessionPackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}