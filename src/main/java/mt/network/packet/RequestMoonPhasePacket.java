package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record RequestMoonPhasePacket() implements CustomPacketPayload {

    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "request_moon_phase");
    public static final Type<@NotNull RequestMoonPhasePacket> TYPE = new Type<>(ID_LOC);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestMoonPhasePacket> CODEC = StreamCodec.unit(new RequestMoonPhasePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}