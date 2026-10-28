package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MoonPhasePacket(int moonPhase) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "moon_phase");
    public static final CustomPacketPayload.Type<MoonPhasePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, MoonPhasePacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.moonPhase()),
            buf -> new MoonPhasePacket(buf.readVarInt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}