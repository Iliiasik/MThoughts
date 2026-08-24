package mt.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailySummaryPacketTest {

    private static final int WIRE_MAXIMUM = Integer.MAX_VALUE / 2;

    private static DailySummaryPacket roundTrip(DailySummaryPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        DailySummaryPacket.CODEC.encode(buf, packet);
        DailySummaryPacket decoded = DailySummaryPacket.CODEC.decode(buf);
        assertEquals(0, buf.readableBytes(),
                "decode did not consume the whole buffer, encode and decode are out of sync");
        return decoded;
    }

    private static DailySummaryPacket.PlayerDailySummary summary(String name) {
        return new DailySummaryPacket.PlayerDailySummary(
                name, 11, 22, 33, 44, 55, 66, true, List.of("distance_record", "mobs_record"));
    }

    @Test
    void anEmptyLobbyRoundTrips() {
        DailySummaryPacket packet = new DailySummaryPacket(List.of());

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void aSinglePlayerRoundTrips() {
        DailySummaryPacket packet = new DailySummaryPacket(List.of(summary("Steve")));

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void everyFieldOfASummaryIsCarried() {
        DailySummaryPacket.PlayerDailySummary decoded =
                roundTrip(new DailySummaryPacket(List.of(summary("Alex")))).summaries().get(0);

        assertEquals("Alex", decoded.playerName());
        assertEquals(11, decoded.blocksDestroyed());
        assertEquals(22, decoded.distanceWalked());
        assertEquals(33, decoded.mobsKilled());
        assertEquals(44, decoded.deaths());
        assertEquals(55, decoded.jumps());
        assertEquals(66, decoded.damageDealt());
        assertTrue(decoded.isMvp());
        assertEquals(List.of("distance_record", "mobs_record"), decoded.achievements());
    }

    @Test
    void distinctStatValuesAreNeverSwapped() {
        DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                "Ordered", 1, 2, 3, 4, 5, 6, false, List.of());

        DailySummaryPacket.PlayerDailySummary decoded =
                roundTrip(new DailySummaryPacket(List.of(original))).summaries().get(0);

        assertEquals(1, decoded.blocksDestroyed());
        assertEquals(2, decoded.distanceWalked());
        assertEquals(3, decoded.mobsKilled());
        assertEquals(4, decoded.deaths());
        assertEquals(5, decoded.jumps());
        assertEquals(6, decoded.damageDealt());
    }

    @Test
    void playerOrderIsPreserved() {
        List<DailySummaryPacket.PlayerDailySummary> players = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            players.add(summary("Player" + i));
        }

        DailySummaryPacket decoded = roundTrip(new DailySummaryPacket(players));

        for (int i = 0; i < 20; i++) {
            assertEquals("Player" + i, decoded.summaries().get(i).playerName(),
                    "player order changed at index " + i);
        }
    }

    @Test
    void negativeStatsAreClampedToZeroOnTheWire() {
        DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                "Negative", -1, -1000, -5, -7, -9, -11, false, List.of());

        DailySummaryPacket.PlayerDailySummary decoded =
                roundTrip(new DailySummaryPacket(List.of(original))).summaries().get(0);

        assertEquals(0, decoded.blocksDestroyed());
        assertEquals(0, decoded.distanceWalked());
        assertEquals(0, decoded.mobsKilled());
        assertEquals(0, decoded.deaths());
        assertEquals(0, decoded.jumps());
        assertEquals(0, decoded.damageDealt());
    }

    @Test
    void oversizedStatsAreClampedToHalfTheIntegerRange() {
        DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                "Huge", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, true, List.of());

        DailySummaryPacket.PlayerDailySummary decoded =
                roundTrip(new DailySummaryPacket(List.of(original))).summaries().get(0);

        assertEquals(WIRE_MAXIMUM, decoded.blocksDestroyed());
        assertEquals(WIRE_MAXIMUM, decoded.distanceWalked());
        assertEquals(WIRE_MAXIMUM, decoded.mobsKilled());
        assertEquals(WIRE_MAXIMUM, decoded.deaths());
        assertEquals(WIRE_MAXIMUM, decoded.jumps());
        assertEquals(WIRE_MAXIMUM, decoded.damageDealt());
    }

    @Test
    void clampingIsStableAcrossRepeatedRoundTrips() {
        DailySummaryPacket packet = new DailySummaryPacket(List.of(
                new DailySummaryPacket.PlayerDailySummary(
                        "Stable", -5, Integer.MAX_VALUE, 100, 0, 7, 9, true, List.of("first"))));

        DailySummaryPacket once = roundTrip(packet);
        DailySummaryPacket twice = roundTrip(once);

        assertEquals(once, twice, "a second round trip changed already clamped values");
    }

    @Test
    void aPlayerWithNoAchievementsRoundTrips() {
        DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                "Empty", 1, 2, 3, 4, 5, 6, false, List.of());

        assertTrue(roundTrip(new DailySummaryPacket(List.of(original)))
                .summaries().get(0).achievements().isEmpty());
    }

    @Test
    void manyAchievementsRoundTrip() {
        List<String> achievements = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            achievements.add("achievement_" + i);
        }
        DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                "Collector", 1, 2, 3, 4, 5, 6, true, achievements);

        assertEquals(achievements, roundTrip(new DailySummaryPacket(List.of(original)))
                .summaries().get(0).achievements());
    }

    @Test
    void theMvpFlagIsCarriedPerPlayer() {
        DailySummaryPacket packet = new DailySummaryPacket(List.of(
                new DailySummaryPacket.PlayerDailySummary("Winner", 1, 1, 1, 0, 1, 1, true, List.of()),
                new DailySummaryPacket.PlayerDailySummary("Other", 1, 1, 1, 0, 1, 1, false, List.of())));

        DailySummaryPacket decoded = roundTrip(packet);

        assertTrue(decoded.summaries().get(0).isMvp());
        assertFalse(decoded.summaries().get(1).isMvp());
    }

    @Test
    void unicodeNamesAndAchievementsRoundTrip() {
        for (String name : List.of("Игрок", "プレイヤー", "플레이어", "Oyuncu", "Gracz")) {
            DailySummaryPacket.PlayerDailySummary original = new DailySummaryPacket.PlayerDailySummary(
                    name, 1, 2, 3, 4, 5, 6, false, List.of("достижение", "実績"));

            DailySummaryPacket.PlayerDailySummary decoded =
                    roundTrip(new DailySummaryPacket(List.of(original))).summaries().get(0);

            assertEquals(name, decoded.playerName());
            assertEquals(List.of("достижение", "実績"), decoded.achievements());
        }
    }

    @Test
    void aFullServerRoundTrips() {
        List<DailySummaryPacket.PlayerDailySummary> players = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            players.add(new DailySummaryPacket.PlayerDailySummary(
                    "Player" + i, i, i * 100, i % 7, i % 3, i * 13, i * 29,
                    i == 0, List.of("a" + i, "b" + i)));
        }

        DailySummaryPacket decoded = roundTrip(new DailySummaryPacket(players));

        assertEquals(200, decoded.summaries().size());
        assertEquals(players, decoded.summaries());
    }
}
