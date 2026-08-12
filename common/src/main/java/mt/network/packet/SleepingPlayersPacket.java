package mt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@org.jetbrains.annotations.NotNull SleepingPlayersPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("midnightthoughts", "sleeping_players"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SleepingPlayersPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SleepingPlayersPacket::sleepingCount,
                    ByteBufCodecs.VAR_INT, SleepingPlayersPacket::totalPlayers,
                    SleepingPlayersPacket::new
            );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
