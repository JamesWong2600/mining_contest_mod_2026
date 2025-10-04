package org.link_uuid.miningContestMod2026.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MsptPackets(int mspt) implements CustomPayload {
    public static final Identifier MSPT_PACKET =
            Identifier.of("mining-contest-mod-2026", "mspt");
    public static final Id<MsptPackets> ID = new Id<>(MSPT_PACKET);

    public static final PacketCodec<RegistryByteBuf, MsptPackets> CODEC =
            PacketCodec.of(MsptPackets::encode, MsptPackets::decode);

    private static void encode(MsptPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.mspt);
    }

    private static MsptPackets decode(RegistryByteBuf buf) {
        return new MsptPackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}