package mt.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DailySummaryPacket(List<PlayerDailySummary> summaries) implements CustomPayload {
    public static final CustomPayload.Id<DailySummaryPacket> ID =
            new CustomPayload.Id<>(Identifier.of("midnightthoughts", "daily_summary"));

    public static final PacketCodec<RegistryByteBuf, DailySummaryPacket> CODEC = new PacketCodec<>() {
        @Override
        public DailySummaryPacket decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            List<PlayerDailySummary> summaries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                summaries.add(PlayerDailySummary.decode(buf));
            }
            return new DailySummaryPacket(summaries);
        }

        @Override
        public void encode(RegistryByteBuf buf, DailySummaryPacket packet) {
            buf.writeVarInt(packet.summaries().size());
            for (PlayerDailySummary summary : packet.summaries()) {
                PlayerDailySummary.encode(buf, summary);
            }
        }
    };

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
            int jumps,
            int damageDealt,
            boolean isMvp,
            List<String> achievements
    ) {
        public static PlayerDailySummary decode(RegistryByteBuf buf) {
            String playerName = buf.readString();
            int blocksDestroyed = clampValue(buf.readVarInt());
            int distanceWalked = clampValue(buf.readVarInt());
            int mobsKilled = clampValue(buf.readVarInt());
            int deaths = clampValue(buf.readVarInt());
            int jumps = clampValue(buf.readVarInt());
            int damageDealt = clampValue(buf.readVarInt());
            boolean isMvp = buf.readBoolean();
            int achievementCount = buf.readVarInt();
            List<String> achievements = new ArrayList<>(achievementCount);
            for (int i = 0; i < achievementCount; i++) {
                achievements.add(buf.readString());
            }
            return new PlayerDailySummary(playerName, blocksDestroyed, distanceWalked, mobsKilled, deaths, jumps, damageDealt, isMvp, achievements);
        }

        public static void encode(RegistryByteBuf buf, PlayerDailySummary summary) {
            buf.writeString(summary.playerName());
            buf.writeVarInt(clampValue(summary.blocksDestroyed()));
            buf.writeVarInt(clampValue(summary.distanceWalked()));
            buf.writeVarInt(clampValue(summary.mobsKilled()));
            buf.writeVarInt(clampValue(summary.deaths()));
            buf.writeVarInt(clampValue(summary.jumps()));
            buf.writeVarInt(clampValue(summary.damageDealt()));
            buf.writeBoolean(summary.isMvp());
            buf.writeVarInt(summary.achievements().size());
            for (String achievement : summary.achievements()) {
                buf.writeString(achievement);
            }
        }

        private static int clampValue(int value) {
            return value < 0 ? 0 : Math.min(value, Integer.MAX_VALUE / 2);
        }
    }
}