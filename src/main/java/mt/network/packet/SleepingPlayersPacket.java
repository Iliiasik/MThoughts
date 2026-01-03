package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) implements CustomPayload {
    public static final CustomPayload.Id<SleepingPlayersPacket> ID =
        new CustomPayload.Id<>(Identifier.of("midnightthoughts", "sleeping_players"));

    public static final PacketCodec<RegistryByteBuf, SleepingPlayersPacket> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, SleepingPlayersPacket::sleepingCount,
            PacketCodecs.VAR_INT, SleepingPlayersPacket::totalPlayers,
            SleepingPlayersPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

