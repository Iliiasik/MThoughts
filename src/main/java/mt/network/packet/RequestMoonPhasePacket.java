package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestMoonPhasePacket() implements CustomPayload {
    public static final CustomPayload.Id<RequestMoonPhasePacket> ID =
            new CustomPayload.Id<>(Identifier.of("midnightthoughts", "request_moon_phase"));

    public static final PacketCodec<RegistryByteBuf, RequestMoonPhasePacket> CODEC =
            PacketCodec.of(
                    (value, buf) -> {},
                    buf -> new RequestMoonPhasePacket()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}