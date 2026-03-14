package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks, int phase, boolean nightmareMode, boolean mvp) {

    public static void encode(WellRestedPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.active());
        buf.writeInt(packet.level());
        buf.writeInt(packet.ticksRemaining());
        buf.writeInt(packet.totalTicks());
        buf.writeInt(packet.phase());
        buf.writeBoolean(packet.nightmareMode());
        buf.writeBoolean(packet.mvp());
    }

    public static WellRestedPacket decode(FriendlyByteBuf buf) {
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