package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks, int phase, boolean nightmareMode, boolean mvp) implements CustomPayload {

    public static final Identifier ID_LOC = Identifier.of("midnightthoughts", "well_rested");
    public static final CustomPayload.Id<WellRestedPacket> ID = new CustomPayload.Id<>(ID_LOC);
    public static final PacketCodec<PacketByteBuf, WellRestedPacket> CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeBoolean(packet.active());
                buf.writeInt(packet.level());
                buf.writeInt(packet.ticksRemaining());
                buf.writeInt(packet.totalTicks());
                buf.writeInt(packet.phase());
                buf.writeBoolean(packet.nightmareMode());
                buf.writeBoolean(packet.mvp());
            },
            buf -> new WellRestedPacket(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}