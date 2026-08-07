package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record MoonPhasePacket(int moonPhase) implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "moon_phase");
    public static final CustomPacketPayload.Type<@NotNull MoonPhasePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);
    public static final StreamCodec<FriendlyByteBuf, MoonPhasePacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.moonPhase()),
            buf -> new MoonPhasePacket(buf.readVarInt())
    );

    @Override
    public @NotNull Type<@NotNull ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}