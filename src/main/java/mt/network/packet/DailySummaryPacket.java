package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DailySummaryPacket(List<PlayerDailySummary> summaries) implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "daily_summary");
    public static final CustomPacketPayload.Type<DailySummaryPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

    public static final StreamCodec<FriendlyByteBuf, DailySummaryPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.summaries().size());
                for (PlayerDailySummary summary : packet.summaries()) {
                    PlayerDailySummary.encode(buf, summary);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<PlayerDailySummary> summaries = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    summaries.add(PlayerDailySummary.decode(buf));
                }
                return new DailySummaryPacket(summaries);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record PlayerDailySummary(String playerName, int blocksDestroyed, int distanceWalked,
                                     int mobsKilled, int deaths, int jumps, int damageDealt, boolean isMvp,
                                     List<String> achievements) {

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
            buf.writeUtf(summary.playerName());
            buf.writeVarInt(clampValue(summary.blocksDestroyed()));
            buf.writeVarInt(clampValue(summary.distanceWalked()));
            buf.writeVarInt(clampValue(summary.mobsKilled()));
            buf.writeVarInt(clampValue(summary.deaths()));
            buf.writeVarInt(clampValue(summary.jumps()));
            buf.writeVarInt(clampValue(summary.damageDealt()));
            buf.writeBoolean(summary.isMvp());
            buf.writeVarInt(summary.achievements().size());
            for (String achievement : summary.achievements()) {
                buf.writeUtf(achievement);
            }
        }

        private static int clampValue(int value) {
            return value < 0 ? 0 : Math.min(value, Integer.MAX_VALUE / 2);
        }
    }
}