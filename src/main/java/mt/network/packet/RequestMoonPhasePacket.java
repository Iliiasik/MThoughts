package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record RequestMoonPhasePacket() implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "request_moon_phase");
    public static final CustomPacketPayload.Type<@NotNull RequestMoonPhasePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);
    public static final StreamCodec<FriendlyByteBuf, RequestMoonPhasePacket> CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new RequestMoonPhasePacket()
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}