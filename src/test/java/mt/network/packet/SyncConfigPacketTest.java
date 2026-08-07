package mt.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncConfigPacketTest {

    private static final SyncConfigPacket BASE = new SyncConfigPacket(
            1000, 2000, 300, 400,
            0.1f, 0.2f, 0.3f, 0.4f,
            true, false, true, false, true, false,
            "magic",
            true, true, false, true, false
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

    private static Object mutate(Object value) {
        if (value instanceof Boolean flag) return !flag;
        if (value instanceof Integer number) return number + 12345;
        if (value instanceof Float number) return number + 0.125f;
        if (value instanceof String text) return text + "_mutated";
        throw new IllegalStateException("unhandled component type " + value.getClass());
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
        int fourFloats = 4 * 4;
        int elevenBooleans = 11;
        int themeLengthPrefix = 1;
        int themeBytes = "magic".getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        assertEquals(fourInts + fourFloats + elevenBooleans + themeLengthPrefix + themeBytes,
                buf.readableBytes(),
                "wire format changed, bump the protocol version before shipping this");
    }

    @Test
    void allFlagsOffSurviveTheRoundTrip() {
        SyncConfigPacket packet = new SyncConfigPacket(
                1, 2, 3, 4,
                0.0f, 0.0f, 0.0f, 0.0f,
                false, false, false, false, false, false,
                "vanilla",
                false, false, false, false, false
        );

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void allFlagsOnSurviveTheRoundTrip() {
        SyncConfigPacket packet = new SyncConfigPacket(
                1, 2, 3, 4,
                1.0f, 1.0f, 1.0f, 1.0f,
                true, true, true, true, true, true,
                "tech",
                true, true, true, true, true
        );

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void extremeNumericValuesSurviveTheRoundTrip() {
        SyncConfigPacket packet = new SyncConfigPacket(
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1,
                Float.MIN_VALUE, Float.MAX_VALUE, -0.0f, 1.0E-10f,
                true, false, true, false, true, false,
                "classic",
                true, true, false, true, false
        );

        assertEquals(packet, roundTrip(packet));
    }

    @Test
    void everyThemeNameSurvivesTheRoundTrip() {
        for (String theme : new String[]{"classic", "magic", "tech", "vanilla"}) {
            SyncConfigPacket packet = new SyncConfigPacket(
                    1000, 2000, 300, 400,
                    0.1f, 0.2f, 0.3f, 0.4f,
                    true, false, true, false, true, false,
                    theme,
                    true, true, false, true, false
            );

            assertEquals(theme, roundTrip(packet).theme());
        }
    }

    @Test
    void anEmptyThemeStringSurvivesTheRoundTrip() {
        SyncConfigPacket packet = new SyncConfigPacket(
                1000, 2000, 300, 400,
                0.1f, 0.2f, 0.3f, 0.4f,
                true, false, true, false, true, false,
                "",
                true, true, false, true, false
        );

        assertEquals("", roundTrip(packet).theme());
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
        assertTrue(BASE.enableOverlay());
    }
}
