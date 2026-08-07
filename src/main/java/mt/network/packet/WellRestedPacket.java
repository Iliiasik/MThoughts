package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks, boolean nightmareMode, boolean mvp) implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested");
    public static final CustomPacketPayload.Type<@NotNull WellRestedPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, WellRestedPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.active());
                buf.writeInt(packet.level());
                buf.writeInt(packet.ticksRemaining());
                buf.writeInt(packet.totalTicks());
                buf.writeBoolean(packet.nightmareMode());
                buf.writeBoolean(packet.mvp());
            },
            buf -> new WellRestedPacket(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    @Override
    public @NotNull Type<@NotNull ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
