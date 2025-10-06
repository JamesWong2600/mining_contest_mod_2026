package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RadiationPackets(double dist) implements CustomPayload {
    public static final Identifier RADIATION_DISTANCE_PACKET =
            Identifier.of("mining_contest_mod_2026", "radiation_distance");
    public static final Id<RadiationPackets> ID = new Id<>(RADIATION_DISTANCE_PACKET);

    public static final PacketCodec<RegistryByteBuf, RadiationPackets> CODEC =
            PacketCodec.of(RadiationPackets::encode, RadiationPackets::decode);

    private static void encode(RadiationPackets packet, RegistryByteBuf buf) {
        buf.writeDouble(packet.dist);
    }

    private static RadiationPackets decode(RegistryByteBuf buf) {
        return new RadiationPackets(buf.readDouble());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}