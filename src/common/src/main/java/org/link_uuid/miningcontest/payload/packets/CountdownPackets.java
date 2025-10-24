package org.link_uuid.miningcontest.payload.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CountdownPackets(int countdown, long timestamp, int session) implements CustomPayload {
    public static final Identifier COUNTDOWN_PACKET =
            Identifier.of("mining_contest_mod_2026", "countdown");
    public static final Id<CountdownPackets> ID = new Id<>(COUNTDOWN_PACKET);

    public static final PacketCodec<RegistryByteBuf, CountdownPackets> CODEC =
            PacketCodec.of(CountdownPackets::encode, CountdownPackets::decode);

    // 無時間戳的建構函數（向後兼容）
    public CountdownPackets(int countdown, int session) {
        this(countdown, System.currentTimeMillis(), session);
    }

    // 無時間戳和session的建構函數（向後兼容）
    public CountdownPackets(int countdown) {
        this(countdown, System.currentTimeMillis(), 0); // 默认session为0
    }

    private static void encode(CountdownPackets packet, RegistryByteBuf buf) {
        buf.writeInt(packet.countdown);
        buf.writeLong(packet.timestamp);
        buf.writeInt(packet.session); // 添加session编码
        System.out.println("Encoded countdown: " + packet.countdown + ", timestamp: " + packet.timestamp + ", session: " + packet.session);
    }

    private static CountdownPackets decode(RegistryByteBuf buf) {
        try {
            int countdown = buf.readInt();
            long timestamp = buf.readLong();
            int session = buf.readInt(); // 添加session解码
            System.out.println("Decoded countdown: " + countdown + ", timestamp: " + timestamp + ", session: " + session);
            return new CountdownPackets(countdown, timestamp, session);
        } catch (Exception e) {
            System.err.println("Error decoding CountdownPackets: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}