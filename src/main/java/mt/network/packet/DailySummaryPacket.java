package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record DailySummaryPacket(List<PlayerDailySummary> summaries) implements CustomPayload {
    public static final CustomPayload.Id<DailySummaryPacket> ID =
        new CustomPayload.Id<>(Identifier.of("midnightthoughts", "daily_summary"));

    public static final PacketCodec<RegistryByteBuf, DailySummaryPacket> CODEC =
        PacketCodec.tuple(
            PlayerDailySummary.CODEC.collect(PacketCodecs.toList()),
            DailySummaryPacket::summaries,
            DailySummaryPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record PlayerDailySummary(
        String playerName,
        int blocksDestroyed,
        int distanceWalked,
        int mobsKilled,
        int deaths,
        int jumps
    ) {
        public static final PacketCodec<RegistryByteBuf, PlayerDailySummary> CODEC =
            PacketCodec.tuple(
                PacketCodecs.STRING, PlayerDailySummary::playerName,
                PacketCodecs.VAR_INT, PlayerDailySummary::blocksDestroyed,
                PacketCodecs.VAR_INT, PlayerDailySummary::distanceWalked,
                PacketCodecs.VAR_INT, PlayerDailySummary::mobsKilled,
                PacketCodecs.VAR_INT, PlayerDailySummary::deaths,
                PacketCodecs.VAR_INT, PlayerDailySummary::jumps,
                PlayerDailySummary::new
            );
    }
}

