package mt.network.packet;

import io.netty.buffer.Unpooled;
import mt.server.AchievementDefinition;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketRoundTripTest {

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }

    private static void assertFullyConsumed(RegistryFriendlyByteBuf buf) {
        assertEquals(0, buf.readableBytes(),
                "decode did not consume the whole buffer, encode and decode are out of sync");
    }

    private static WellRestedPacket roundTrip(WellRestedPacket packet) {
        RegistryFriendlyByteBuf buf = buffer();
        WellRestedPacket.CODEC.encode(buf, packet);
        WellRestedPacket decoded = WellRestedPacket.CODEC.decode(buf);
        assertFullyConsumed(buf);
        return decoded;
    }

    private static MoonPhasePacket roundTrip(MoonPhasePacket packet) {
        RegistryFriendlyByteBuf buf = buffer();
        MoonPhasePacket.CODEC.encode(buf, packet);
        MoonPhasePacket decoded = MoonPhasePacket.CODEC.decode(buf);
        assertFullyConsumed(buf);
        return decoded;
    }

    private static SleepingPlayersPacket roundTrip(SleepingPlayersPacket packet) {
        RegistryFriendlyByteBuf buf = buffer();
        SleepingPlayersPacket.CODEC.encode(buf, packet);
        SleepingPlayersPacket decoded = SleepingPlayersPacket.CODEC.decode(buf);
        assertFullyConsumed(buf);
        return decoded;
    }

    private static UserContentPacket roundTrip(UserContentPacket packet) {
        RegistryFriendlyByteBuf buf = buffer();
        UserContentPacket.CODEC.encode(buf, packet);
        UserContentPacket decoded = UserContentPacket.CODEC.decode(buf);
        assertFullyConsumed(buf);
        return decoded;
    }

    private static SyncAchievementsPacket roundTrip(SyncAchievementsPacket packet) {
        RegistryFriendlyByteBuf buf = buffer();
        SyncAchievementsPacket.CODEC.encode(buf, packet);
        SyncAchievementsPacket decoded = SyncAchievementsPacket.CODEC.decode(buf);
        assertFullyConsumed(buf);
        return decoded;
    }

    private static AchievementDefinition definition(String id, String name, String tooltip) {
        AchievementDefinition definition = new AchievementDefinition();
        definition.id = id;
        definition.name = name;
        definition.tooltip = tooltip;
        return definition;
    }

    @Test
    void wellRestedRoundTripsWithDistinctValues() {
        WellRestedPacket packet = new WellRestedPacket(true, 3, 1200, 2400, false, true);

        WellRestedPacket decoded = roundTrip(packet);

        assertEquals(packet, decoded);
        assertTrue(decoded.active());
        assertEquals(3, decoded.level());
        assertEquals(1200, decoded.ticksRemaining());
        assertEquals(2400, decoded.totalTicks());
        assertFalse(decoded.nightmareMode());
        assertTrue(decoded.mvp());
    }

    @Test
    void wellRestedFlagsAreNeverSwapped() {
        assertEquals(new WellRestedPacket(true, 1, 2, 3, false, false),
                roundTrip(new WellRestedPacket(true, 1, 2, 3, false, false)));
        assertEquals(new WellRestedPacket(false, 1, 2, 3, true, false),
                roundTrip(new WellRestedPacket(false, 1, 2, 3, true, false)));
        assertEquals(new WellRestedPacket(false, 1, 2, 3, false, true),
                roundTrip(new WellRestedPacket(false, 1, 2, 3, false, true)));
    }

    @Test
    void wellRestedCountersAreNeverSwapped() {
        WellRestedPacket decoded = roundTrip(new WellRestedPacket(true, 1, 2, 3, true, true));

        assertEquals(1, decoded.level());
        assertEquals(2, decoded.ticksRemaining());
        assertEquals(3, decoded.totalTicks());
    }

    @Test
    void wellRestedHandlesEveryLevel() {
        for (int level = 1; level <= 5; level++) {
            WellRestedPacket packet = new WellRestedPacket(true, level, 100, 200, false, false);
            assertEquals(packet, roundTrip(packet));
        }
    }

    @Test
    void wellRestedCarriesNoEffectPhase() {
        assertEquals(6, WellRestedPacket.class.getRecordComponents().length,
                "the client never reads an effect phase, it must not travel over the wire");
    }

    @Test
    void wellRestedWireFormatHasTheExpectedSize() {
        RegistryFriendlyByteBuf buf = buffer();
        WellRestedPacket.CODEC.encode(buf, new WellRestedPacket(true, 1, 2, 3, false, true));

        assertEquals(3 + 3 * 4, buf.readableBytes(),
                "wire format changed, bump the protocol version before shipping this");
    }

    @Test
    void moonPhaseRoundTripsForEveryPhase() {
        for (int phase = 0; phase < 8; phase++) {
            assertEquals(phase, roundTrip(new MoonPhasePacket(phase)).moonPhase());
        }
    }

    @Test
    void moonPhaseRoundTripsForOutOfRangeValues() {
        for (int phase : new int[]{-1, 8, 99, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertEquals(phase, roundTrip(new MoonPhasePacket(phase)).moonPhase());
        }
    }

    @Test
    void sleepingPlayersCountsAreNeverSwapped() {
        SleepingPlayersPacket decoded = roundTrip(new SleepingPlayersPacket(2, 7));

        assertEquals(2, decoded.sleepingCount());
        assertEquals(7, decoded.totalPlayers());
    }

    @Test
    void sleepingPlayersHandlesAnEmptyServer() {
        assertEquals(new SleepingPlayersPacket(0, 0), roundTrip(new SleepingPlayersPacket(0, 0)));
    }

    @Test
    void requestMoonPhaseCarriesNoPayload() {
        RegistryFriendlyByteBuf buf = buffer();
        RequestMoonPhasePacket.CODEC.encode(buf, new RequestMoonPhasePacket());

        assertEquals(0, buf.readableBytes(), "the request packet must carry no payload");
        assertNotNull(RequestMoonPhasePacket.CODEC.decode(buf));
    }

    @Test
    void userContentRoundTripsAMultiCategoryMap() {
        Map<String, List<String>> content = new LinkedHashMap<>();
        content.put("facts", List.of("first fact", "second fact"));
        content.put("lore", List.of("a piece of lore"));
        content.put("nightmares", List.of());

        UserContentPacket decoded = roundTrip(new UserContentPacket(content));

        assertEquals(content, decoded.content());
        assertEquals(List.of("first fact", "second fact"), decoded.content().get("facts"));
        assertTrue(decoded.content().get("nightmares").isEmpty());
    }

    @Test
    void userContentRoundTripsAnEmptyMap() {
        assertTrue(roundTrip(new UserContentPacket(Map.of())).content().isEmpty());
    }

    @Test
    void userContentRoundTripsUnicodeAndLongEntries() {
        Map<String, List<String>> content = new LinkedHashMap<>();
        content.put("facts", List.of("Кошки спят 70% жизни", "猫は一日の70%を眠る"));
        content.put("surreal", List.of("a".repeat(4000)));

        assertEquals(content, roundTrip(new UserContentPacket(content)).content());
    }

    @Test
    void userContentRoundTripsALargePayload() {
        Map<String, List<String>> content = new LinkedHashMap<>();
        for (String category : new String[]{"facts", "lore", "nightmares", "special", "surreal"}) {
            List<String> entries = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                entries.add(category + " entry number " + i);
            }
            content.put(category, entries);
        }

        UserContentPacket decoded = roundTrip(new UserContentPacket(content));

        assertEquals(content, decoded.content());
        assertEquals(5, decoded.content().size());
    }

    @Test
    void achievementsRoundTripFieldByField() {
        List<AchievementDefinition> original = List.of(
                definition("first", "First Sleep", "Sleep for the first time"),
                definition("second", "Second Wind", "Another tooltip"));

        List<AchievementDefinition> decoded =
                roundTrip(new SyncAchievementsPacket(original)).achievements();

        assertEquals(2, decoded.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).id, decoded.get(i).id);
            assertEquals(original.get(i).name, decoded.get(i).name);
            assertEquals(original.get(i).tooltip, decoded.get(i).tooltip);
        }
    }

    @Test
    void achievementsRoundTripAnEmptyList() {
        assertTrue(roundTrip(new SyncAchievementsPacket(List.of())).achievements().isEmpty());
    }

    @Test
    void aNullTooltipStaysNull() {
        List<AchievementDefinition> decoded =
                roundTrip(new SyncAchievementsPacket(List.of(definition("id", "Name", null)))).achievements();

        assertNull(decoded.get(0).tooltip);
        assertEquals("id", decoded.get(0).id);
        assertEquals("Name", decoded.get(0).name);
    }

    @Test
    void anEmptyTooltipArrivesAsNull() {
        List<AchievementDefinition> decoded =
                roundTrip(new SyncAchievementsPacket(List.of(definition("id", "Name", "")))).achievements();

        assertNull(decoded.get(0).tooltip,
                "an empty tooltip is indistinguishable from a missing one on the wire");
    }

    @Test
    void conditionsAreNotSentToTheClient() {
        AchievementDefinition definition = definition("id", "Name", "Tooltip");
        definition.conditions = new AchievementDefinition.Conditions();
        definition.conditions.mobsKilledMin = 10;

        List<AchievementDefinition> decoded =
                roundTrip(new SyncAchievementsPacket(List.of(definition))).achievements();

        assertNull(decoded.get(0).conditions, "unlock conditions must stay on the server");
    }

    @Test
    void achievementOrderIsPreserved() {
        List<AchievementDefinition> original = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            original.add(definition("id_" + i, "Name " + i, "Tooltip " + i));
        }

        List<AchievementDefinition> decoded =
                roundTrip(new SyncAchievementsPacket(original)).achievements();

        for (int i = 0; i < 100; i++) {
            assertEquals("id_" + i, decoded.get(i).id, "achievement order changed at index " + i);
        }
    }

    @Test
    void everyPacketSurvivesRepeatedRoundTrips() {
        WellRestedPacket wellRested = new WellRestedPacket(true, 4, 900, 1800, true, false);
        MoonPhasePacket moon = new MoonPhasePacket(5);
        SleepingPlayersPacket sleeping = new SleepingPlayersPacket(3, 9);

        for (int i = 0; i < 20_000; i++) {
            wellRested = roundTrip(wellRested);
            moon = roundTrip(moon);
            sleeping = roundTrip(sleeping);
        }

        assertEquals(new WellRestedPacket(true, 4, 900, 1800, true, false), wellRested);
        assertEquals(new MoonPhasePacket(5), moon);
        assertEquals(new SleepingPlayersPacket(3, 9), sleeping);
    }
}
