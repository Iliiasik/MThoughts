package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DailySummaryPacket(List<PlayerDailySummary> summaries) {
    public static final Identifier ID = Identifier.of("midnightthoughts", "daily_summary");

    public static void encode(DailySummaryPacket packet, PacketByteBuf buf) {
        buf.writeVarInt(packet.summaries.size());
        for (PlayerDailySummary summary : packet.summaries) {
            PlayerDailySummary.encode(buf, summary);
        }
    }

    public static DailySummaryPacket decode(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<PlayerDailySummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(PlayerDailySummary.decode(buf));
        }
        return new DailySummaryPacket(summaries);
    }

    public record PlayerDailySummary(String playerName, int blocksDestroyed, int distanceWalked, int mobsKilled,
                                     int deaths, int jumps, int damageDealt, boolean isMvp, List<String> achievements) {

        public static PlayerDailySummary decode(PacketByteBuf buf) {
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
                return new PlayerDailySummary(playerName, blocksDestroyed, distanceWalked,
                        mobsKilled, deaths, jumps, damageDealt, isMvp, achievements);
            }

            public static void encode(PacketByteBuf buf, PlayerDailySummary summary) {
                buf.writeString(summary.playerName);
                buf.writeVarInt(clampValue(summary.blocksDestroyed));
                buf.writeVarInt(clampValue(summary.distanceWalked));
                buf.writeVarInt(clampValue(summary.mobsKilled));
                buf.writeVarInt(clampValue(summary.deaths));
                buf.writeVarInt(clampValue(summary.jumps));
                buf.writeVarInt(clampValue(summary.damageDealt));
                buf.writeBoolean(summary.isMvp);
                buf.writeVarInt(summary.achievements.size());
                for (String achievement : summary.achievements) {
                    buf.writeString(achievement);
                }
            }

            private static int clampValue(int value) {
                return value < 0 ? 0 : Math.min(value, Integer.MAX_VALUE / 2);
            }
        }
}