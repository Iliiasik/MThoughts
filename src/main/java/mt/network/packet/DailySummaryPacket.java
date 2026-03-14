package mt.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "daily_summary");
    private final List<PlayerDailySummary> summaries;

    public DailySummaryPacket(List<PlayerDailySummary> summaries) {
        this.summaries = summaries;
    }

    public List<PlayerDailySummary> summaries() {
        return summaries;
    }

    public static void encode(DailySummaryPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.summaries.size());
        for (PlayerDailySummary summary : packet.summaries) {
            PlayerDailySummary.encode(buf, summary);
        }
    }

    public static DailySummaryPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<PlayerDailySummary> summaries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            summaries.add(PlayerDailySummary.decode(buf));
        }
        return new DailySummaryPacket(summaries);
    }

    public static class PlayerDailySummary {
        private final String playerName;
        private final int blocksDestroyed;
        private final int distanceWalked;
        private final int mobsKilled;
        private final int deaths;
        private final int jumps;
        private final int damageDealt;
        private final boolean isMvp;
        private final List<String> achievements;

        public PlayerDailySummary(String playerName, int blocksDestroyed, int distanceWalked, int mobsKilled, int deaths, int jumps, int damageDealt, boolean isMvp, List<String> achievements) {
            this.playerName = playerName;
            this.blocksDestroyed = blocksDestroyed;
            this.distanceWalked = distanceWalked;
            this.mobsKilled = mobsKilled;
            this.deaths = deaths;
            this.jumps = jumps;
            this.damageDealt = damageDealt;
            this.isMvp = isMvp;
            this.achievements = achievements;
        }

        public String playerName() { return playerName; }
        public int blocksDestroyed() { return blocksDestroyed; }
        public int distanceWalked() { return distanceWalked; }
        public int mobsKilled() { return mobsKilled; }
        public int deaths() { return deaths; }
        public int jumps() { return jumps; }
        public int damageDealt() { return damageDealt; }
        public boolean isMvp() { return isMvp; }
        public List<String> achievements() { return achievements; }

        public static PlayerDailySummary decode(FriendlyByteBuf buf) {
            String playerName = buf.readUtf();
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
                achievements.add(buf.readUtf());
            }
            return new PlayerDailySummary(playerName, blocksDestroyed, distanceWalked, mobsKilled, deaths, jumps, damageDealt, isMvp, achievements);
        }

        public static void encode(FriendlyByteBuf buf, PlayerDailySummary summary) {
            buf.writeUtf(summary.playerName);
            buf.writeVarInt(clampValue(summary.blocksDestroyed));
            buf.writeVarInt(clampValue(summary.distanceWalked));
            buf.writeVarInt(clampValue(summary.mobsKilled));
            buf.writeVarInt(clampValue(summary.deaths));
            buf.writeVarInt(clampValue(summary.jumps));
            buf.writeVarInt(clampValue(summary.damageDealt));
            buf.writeBoolean(summary.isMvp);
            buf.writeVarInt(summary.achievements.size());
            for (String achievement : summary.achievements) {
                buf.writeUtf(achievement);
            }
        }

        private static int clampValue(int value) {
            return value < 0 ? 0 : Math.min(value, Integer.MAX_VALUE / 2);
        }
    }
}