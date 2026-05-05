package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record MoonPhasePacket(int moonPhase) implements CustomPayload {

    public static final Identifier ID_LOC = Identifier.of("midnightthoughts", "moon_phase");
    public static final Id<MoonPhasePacket> ID = new Id<>(ID_LOC);

    public static final PacketCodec<PacketByteBuf, MoonPhasePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, MoonPhasePacket::moonPhase,
            MoonPhasePacket::new
    );

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}