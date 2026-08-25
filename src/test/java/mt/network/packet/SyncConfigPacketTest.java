package mt.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncConfigPacketTest {

    private static final SyncConfigPacket BASE = new SyncConfigPacket(
            1000, 2000, 300, 400,
            0.1f, 0.2f, 0.3f, 0.4f,
            true, false, true, false, true, false,
            "magic",
            "bar",
            true, true, false, true, false,
            true, 1.5f, 0.75f
    );

    private static SyncConfigPacket roundTrip(SyncConfigPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        SyncConfigPacket.encode(packet, buf);
        SyncConfigPacket decoded = SyncConfigPacket.decode(buf);
        assertEquals(0, buf.readableBytes(),
                "decode did not consume the whole buffer, encode and decode are out of sync");
        return decoded;
    }

    private static Object[] componentsOf(SyncConfigPacket packet) throws Exception {
        RecordComponent[] components = SyncConfigPacket.class.getRecordComponents();
        Object[] values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            values[i] = components[i].getAccessor().invoke(packet);
        }
        return values;
    }

    private static SyncConfigPacket construct(Object[] values) throws Exception {
        RecordComponent[] components = SyncConfigPacket.class.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
        }
        return SyncConfigPacket.class.getDeclaredConstructor(types).newInstance(values);
    }

    private static SyncConfigPacket withFlags(boolean value) {
        return new SyncConfigPacket(
                1, 2, 3, 4,
                0.0f, 0.5f, 1.0f, 0.25f,
                value, value, value, value, value, value,
                "magic", "bar",
                value, value, value, value, value,
                value, 0.5f, 1.5f
        );
    }

    private static SyncConfigPacket withTheme(String theme) {
        return new SyncConfigPacket(
                1000, 2000, 300, 400,
                0.1f, 0.2f, 0.3f, 0.4f,
                true, false, true, false, true, false,
                theme, "bar",
                true, true, false, true, false,
                true, 1.25f, 0.5f
        );
    }

    private static SyncConfigPacket withHudPosition(String position) {
        return new SyncConfigPacket(
                1000, 2000, 300, 400,
                0.1f, 0.2f, 0.3f, 0.4f,
                true, false, true, false, true, false,
                "magic", position,
                true, true, false, true, false,
                true, 1.25f, 0.5f
        );
    }

    private static Object mutate(Object value) {
        if (value instanceof Boolean flag) return !flag;
        if (value instanceof Integer number) return number + 12345;
        if (value instanceof Float number) return number + 0.125f;
        if (value instanceof String text) return text + "_mutated";
        throw new IllegalStateException("unhandled component type " + value.getClass());
    }

    @Test
    void theRecordStillCarriesEveryConfiguredValue() {
        assertEquals(24, SyncConfigPacket.class.getRecordComponents().length,
                "a config field was added or removed without updating the wire format");
    }

    @Test
    void roundTripPreservesEveryValue() {
        assertEquals(BASE, roundTrip(BASE));
    }

    @Test
    void everyFieldIsCarriedIndependently() throws Exception {
        SyncConfigPacket baseline = roundTrip(BASE);
        Object[] baselineValues = componentsOf(baseline);
        RecordComponent[] components = SyncConfigPacket.class.getRecordComponents();

        for (int i = 0; i < components.length; i++) {
            Object[] mutated = componentsOf(BASE);
            mutated[i] = mutate(mutated[i]);

            SyncConfigPacket decoded = roundTrip(construct(mutated));
            Object[] decodedValues = componentsOf(decoded);

            assertNotEquals(baseline, decoded,
                    "changing " + components[i].getName() + " had no effect after a round trip");

            for (int j = 0; j < components.length; j++) {
                if (i == j) {
                    assertNotEquals(baselineValues[j], decodedValues[j],
                            components[i].getName() + " was not carried over the wire");
                } else {
                    assertEquals(baselineValues[j], decodedValues[j],
                            components[j].getName() + " changed when only "
                                    + components[i].getName() + " was modified");
                }
            }
        }
    }

    @Test
    void wireFormatHasTheExpectedSize() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        SyncConfigPacket.encode(BASE, buf);

        int fourInts = 4 * 4;
        int sixFloats = 6 * 4;
        int twelveBooleans = 12;
        int theme = 1 + "magic".getBytes(StandardCharsets.UTF_8).length;
        int hudPosition = 1 + "bar".getBytes(StandardCharsets.UTF_8).length;

        assertEquals(fourInts + sixFloats + twelveBooleans + theme + hudPosition,
                buf.readableBytes(),
                "wire format changed, bump the protocol version before shipping this");
    }

    @Test
    void allFlagsOffSurviveTheRoundTrip() {
        assertEquals(withFlags(false), roundTrip(withFlags(false)));
    }

    @Test
    void allFlagsOnSurviveTheRoundTrip() {
        assertEquals(withFlags(true), roundTrip(withFlags(true)));
    }

    @Test
    void extremeNumericValuesSurviveTheRoundTrip() {
        SyncConfigPacket packet = new SyncConfigPacket(
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1,
                Float.MIN_VALUE, Float.MAX_VALUE, -0.0f, 1.0E-10f,
                true, false, true, false, true, false,
                "classic", "left",
                true, true, false, true, false,
                true, 1.25f, 0.5f
        );

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void everyThemeNameSurvivesTheRoundTrip() {
        for (String theme : new String[]{"classic", "magic", "tech", "vanilla"}) {
            assertEquals(theme, roundTrip(withTheme(theme)).theme());
        }
    }

    @Test
    void allHudPositionsSurviveTheRoundTrip() {
        for (String position : new String[]{"left", "bar", "right"}) {
            assertEquals(position, roundTrip(withHudPosition(position)).wellRestedHudPosition());
        }
    }

    @Test
    void emptyStringsSurviveTheRoundTrip() {
        assertEquals("", roundTrip(withTheme("")).theme());
        assertEquals("", roundTrip(withHudPosition("")).wellRestedHudPosition());
    }

    @Test
    void repeatedRoundTripsAreStable() {
        SyncConfigPacket current = BASE;
        for (int i = 0; i < 10_000; i++) {
            current = roundTrip(current);
        }
        assertEquals(BASE, current);
    }

    @Test
    void encodingDoesNotMutateTheSourcePacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        SyncConfigPacket.encode(BASE, buf);

        assertEquals(1000, BASE.minSlideDisplayTimeMs());
        assertEquals("magic", BASE.theme());
        assertEquals("bar", BASE.wellRestedHudPosition());
        assertTrue(BASE.enableOverlay());
    }
}
