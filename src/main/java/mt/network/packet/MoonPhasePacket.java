package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MoonPhasePacket(int moonPhase) implements CustomPayload {
    public static final CustomPayload.Id<MoonPhasePacket> ID =
            new CustomPayload.Id<>(Identifier.of("midnightthoughts", "moon_phase"));

    public static final PacketCodec<RegistryByteBuf, MoonPhasePacket> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeVarInt(value.moonPhase()),
                    buf -> new MoonPhasePacket(buf.readVarInt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}