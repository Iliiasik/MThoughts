package mt.client.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberFormatterTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 1",
            "999, 999",
            "1000, 1K",
            "1100, 1.1K",
            "1500, 1.5K",
            "9999, 10K",
            "999999, 1000K",
            "1000000, 1M",
            "1500000, 1.5M",
            "999999999, 1000M",
            "1000000000, 1B",
            "2147483647, 2.1B"
    })
    void formatsLargeNumbers(long input, String expected) {
        assertEquals(expected, NumberFormatter.formatLargeNumber(input));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, -1",
            "-999, -999",
            "-1000, -1K",
            "-1500000, -1.5M"
    })
    void formatsNegativeNumbers(long input, String expected) {
        assertEquals(expected, NumberFormatter.formatLargeNumber(input));
    }

    @Test
    void formattingIsLocaleIndependent() {
        assertTrue(NumberFormatter.formatLargeNumber(1500).contains("."),
                "decimal separator must be a dot regardless of the system locale");
        assertFalse(NumberFormatter.formatLargeNumber(1500).contains(","));
    }

    @Test
    void intOverloadMatchesLongOverload() {
        int[] samples = {0, 7, 999, 1000, 54321, 1_000_000, Integer.MAX_VALUE, -1, -999_999, Integer.MIN_VALUE + 1};
        for (int sample : samples) {
            assertEquals(NumberFormatter.formatLargeNumber((long) sample),
                    NumberFormatter.formatLargeNumber(sample),
                    "overloads disagree for " + sample);
        }
    }

    @Test
    void formattingNeverProducesAnEmptyOrOversizedLabel() {
        for (long value = 0; value < 2_000_000L; value += 977) {
            String formatted = NumberFormatter.formatLargeNumber(value);
            assertFalse(formatted.isEmpty(), "empty label for " + value);
            assertTrue(formatted.length() <= 7, "label too wide for the badge: " + formatted);
        }
    }

    @Test
    void formattingIsMonotonicInLength() {
        assertEquals(3, NumberFormatter.formatLargeNumber(999).length());
        assertEquals(2, NumberFormatter.formatLargeNumber(1000).length());
        assertEquals(2, NumberFormatter.formatLargeNumber(1_000_000).length());
        assertEquals(2, NumberFormatter.formatLargeNumber(1_000_000_000L).length());
    }

    @Test
    void formattingUnderLoadStaysCorrect() {
        for (int i = 0; i < 500_000; i++) {
            assertEquals("1.5K", NumberFormatter.formatLargeNumber(1500));
        }
    }
}
