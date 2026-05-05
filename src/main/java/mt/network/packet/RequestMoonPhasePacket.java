package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record RequestMoonPhasePacket() implements CustomPayload {

    public static final Identifier ID_LOC = Identifier.of("midnightthoughts", "request_moon_phase");
    public static final Id<@NotNull RequestMoonPhasePacket> ID = new Id<>(ID_LOC);

    public static final PacketCodec<PacketByteBuf, RequestMoonPhasePacket> CODEC =
            PacketCodec.unit(new RequestMoonPhasePacket());

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}