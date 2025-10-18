package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TimerPackets(int timer) implements CustomPayload {
    public static final Identifier TIMER_PACKET =
            Identifier.of("mining_contest_mod_2026", "timer");
    public static final Id<TimerPackets> ID = new Id<>(TIMER_PACKET);

    public static final PacketCodec<RegistryByteBuf, TimerPackets> CODEC =
            PacketCodec.of(TimerPackets::encode, TimerPackets::decode);

    private static void encode(TimerPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.timer);
    }

    private static TimerPackets decode(RegistryByteBuf buf) {
        return new TimerPackets(buf.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}