package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PVPModePacket(int mode_index) implements CustomPayload {
    public static final Identifier PVP_MODE_PACKET =
            Identifier.of("mining_contest_mod_2026", "pvp_mode_packet");
    public static final Id<PVPModePacket> ID = new Id<>(PVP_MODE_PACKET);

    public static final PacketCodec<RegistryByteBuf, PVPModePacket> CODEC =
            PacketCodec.of(PVPModePacket::encode, PVPModePacket::decode);

    private static void encode(PVPModePacket packet, RegistryByteBuf buf) {
        buf.writeInt(packet.mode_index);
    }

    private static PVPModePacket decode(RegistryByteBuf buf) {
        return new PVPModePacket(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}