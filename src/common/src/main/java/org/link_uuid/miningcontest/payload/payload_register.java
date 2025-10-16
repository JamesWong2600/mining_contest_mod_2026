package org.link_uuid.miningcontest.payload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.link_uuid.miningcontest.payload.packets.*;

public class payload_register {
    public static void payload_register_init() {
        PayloadTypeRegistry.playS2C().register(
        RadiationPackets.ID,
        RadiationPackets.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
        SessionPackets.ID,
        SessionPackets.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
        PingPackets.ID,
        PingPackets.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
        MsptPackets.ID,
        MsptPackets.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
        PlayerAmountPackets.ID,
        PlayerAmountPackets.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
        PVPModePacket.ID,
        PVPModePacket.CODEC
        );
    }
}
