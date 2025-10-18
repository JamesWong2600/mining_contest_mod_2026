package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ScorePackets(int score) implements CustomPayload {
    public static final Identifier SCORE_PACKET =
            Identifier.of("mining_contest_mod_2026", "score");
    public static final Id<ScorePackets> ID = new Id<>(SCORE_PACKET);

    public static final PacketCodec<RegistryByteBuf, ScorePackets> CODEC =
            PacketCodec.of(ScorePackets::encode, ScorePackets::decode);

    private static void encode(ScorePackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.score);
    }

    private static ScorePackets decode(RegistryByteBuf buf) {
        return new ScorePackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}