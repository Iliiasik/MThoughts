package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks, boolean nightmareMode, boolean mvp) implements CustomPacketPayload {

    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested");
    public static final CustomPacketPayload.Type<@org.jetbrains.annotations.NotNull WellRestedPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);
    public static final StreamCodec<RegistryFriendlyByteBuf, WellRestedPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, WellRestedPacket::active,
            ByteBufCodecs.INT, WellRestedPacket::level,
            ByteBufCodecs.INT, WellRestedPacket::ticksRemaining,
            ByteBufCodecs.INT, WellRestedPacket::totalTicks,
            ByteBufCodecs.BOOL, WellRestedPacket::nightmareMode,
            ByteBufCodecs.BOOL, WellRestedPacket::mvp,
            WellRestedPacket::new
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
