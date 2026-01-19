package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryPacket {
    public static final Identifier ID = new Identifier("midnightthoughts", "daily_summary");

    private final List<PlayerDailySummary> summaries;

    public DailySummaryPacket(List<PlayerDailySummary> summaries) {
        this.summaries = summaries;
    }

    public List<PlayerDailySummary> summaries() {
        return summaries;
    }

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

    public static class PlayerDailySummary {
        private final String playerName;
        private final int blocksDestroyed;
        private final int distanceWalked;
        private final int mobsKilled;
        private final int deaths;
        private final int jumps;
        private final boolean isMvp;
        private final List<String> achievements;

        public PlayerDailySummary(String playerName, int blocksDestroyed, int distanceWalked, int mobsKilled, int deaths, int jumps, boolean isMvp, List<String> achievements) {
            this.playerName = playerName;
            this.blocksDestroyed = blocksDestroyed;
            this.distanceWalked = distanceWalked;
            this.mobsKilled = mobsKilled;
            this.deaths = deaths;
            this.jumps = jumps;
            this.isMvp = isMvp;
            this.achievements = achievements;
        }

        public String playerName() {
            return playerName;
        }

        public int blocksDestroyed() {
            return blocksDestroyed;
        }

        public int distanceWalked() {
            return distanceWalked;
        }

        public int mobsKilled() {
            return mobsKilled;
        }

        public int deaths() {
            return deaths;
        }

        public int jumps() {
            return jumps;
        }

        public boolean isMvp() {
            return isMvp;
        }

        public List<String> achievements() {
            return achievements;
        }

        public static PlayerDailySummary decode(PacketByteBuf buf) {
            String playerName = buf.readString();
            int blocksDestroyed = buf.readVarInt();
            int distanceWalked = buf.readVarInt();
            int mobsKilled = buf.readVarInt();
            int deaths = buf.readVarInt();
            int jumps = buf.readVarInt();
            boolean isMvp = buf.readBoolean();
            int achievementCount = buf.readVarInt();
            List<String> achievements = new ArrayList<>(achievementCount);
            for (int i = 0; i < achievementCount; i++) {
                achievements.add(buf.readString());
            }
            return new PlayerDailySummary(playerName, blocksDestroyed, distanceWalked, mobsKilled, deaths, jumps, isMvp, achievements);
        }

        public static void encode(PacketByteBuf buf, PlayerDailySummary summary) {
            buf.writeString(summary.playerName);
            buf.writeVarInt(summary.blocksDestroyed);
            buf.writeVarInt(summary.distanceWalked);
            buf.writeVarInt(summary.mobsKilled);
            buf.writeVarInt(summary.deaths);
            buf.writeVarInt(summary.jumps);
            buf.writeBoolean(summary.isMvp);
            buf.writeVarInt(summary.achievements.size());
            for (String achievement : summary.achievements) {
                buf.writeString(achievement);
            }
        }
    }
}