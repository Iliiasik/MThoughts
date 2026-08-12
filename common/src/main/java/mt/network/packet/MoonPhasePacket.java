package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record MoonPhasePacket(int moonPhase) implements CustomPacketPayload {

    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "moon_phase");
    public static final Type<@NotNull MoonPhasePacket> TYPE = new Type<>(ID_LOC);
    public static final StreamCodec<RegistryFriendlyByteBuf, MoonPhasePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MoonPhasePacket::moonPhase,
            MoonPhasePacket::new
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
