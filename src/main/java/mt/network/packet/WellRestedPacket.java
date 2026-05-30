package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks, int phase,
                               boolean nightmareMode, boolean mvp) {
    public static final Identifier ID = Identifier.of("midnightthoughts", "well_rested");

    public static void encode(WellRestedPacket packet, PacketByteBuf buf) {
        buf.writeBoolean(packet.active);
        buf.writeInt(packet.level);
        buf.writeInt(packet.ticksRemaining);
        buf.writeInt(packet.totalTicks);
        buf.writeInt(packet.phase);
        buf.writeBoolean(packet.nightmareMode);
        buf.writeBoolean(packet.mvp);
    }

    public static WellRestedPacket decode(PacketByteBuf buf) {
        return new WellRestedPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }
}