package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PlayerSleepStatePacket(boolean active, int level, int ticksRemaining, int totalTicks, boolean nightmareMode, boolean mvp) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "player_sleep_state");
    public static final CustomPacketPayload.Type<PlayerSleepStatePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, PlayerSleepStatePacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.active());
                buf.writeInt(packet.level());
                buf.writeInt(packet.ticksRemaining());
                buf.writeInt(packet.totalTicks());
                buf.writeBoolean(packet.nightmareMode());
                buf.writeBoolean(packet.mvp());
            },
            buf -> new PlayerSleepStatePacket(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
