package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record MoonPhasePacket(int moonPhase) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "moon_phase");
    public static final CustomPacketPayload.Type<MoonPhasePacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, MoonPhasePacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeVarInt(packet.moonPhase()),
            buf -> new MoonPhasePacket(buf.readVarInt())
    );

    public static MoonPhasePacket current(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return new MoonPhasePacket(overworld != null ? overworld.getMoonPhase() : 0);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
