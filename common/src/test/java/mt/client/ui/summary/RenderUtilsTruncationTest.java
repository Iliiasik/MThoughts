package mt.client.ui.summary;

import mt.support.StubFont;
import net.minecraft.client.gui.Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderUtilsTruncationTest {

    private Font font;

    @BeforeEach
    void setUp() {
        font = StubFont.fixedWidth(6);
    }

    private String truncate(String text, int maxWidth) {
        return RenderUtils.truncateWithEllipsis(font, text, maxWidth);
    }

    @Test
    void nullAndEmptyTextArePassedThrough() {
        assertNull(truncate(null, 100));
        assertEquals("", truncate("", 100));
    }

    @Test
    void textThatFitsIsReturnedUnchanged() {
        String text = "Steve";
        assertSame(text, truncate(text, 1000));
        assertSame(text, truncate(text, text.length() * 6));
    }

    @Test
    void textThatDoesNotFitIsTruncatedWithAnEllipsis() {
        String result = truncate("AVeryLongPlayerName", 60);

        assertTrue(result.endsWith("..."), "truncated text must end with an ellipsis: " + result);
        assertTrue(result.length() * 6 <= 60, "truncated text still overflows: " + result);
        assertEquals("AVeryLon...", truncate("AVeryLongPlayerName", 66));
    }

    @Test
    void resultNeverExceedsTheAvailableWidth() {
        String text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int maxWidth = 1; maxWidth <= 400; maxWidth++) {
            String result = truncate(text, maxWidth);
            if (maxWidth >= 18) {
                assertTrue(font.width(result) <= maxWidth,
                        "overflow at maxWidth " + maxWidth + ": \"" + result + "\"");
            }
        }
    }

    @Test
    void aWidthTooSmallForTheEllipsisYieldsJustTheEllipsis() {
        assertEquals("...", truncate("Steve", 18));
        assertEquals("...", truncate("Steve", 10));
        assertEquals("...", truncate("Steve", 0));
        assertEquals("...", truncate("Steve", -50));
    }

    @Test
    void truncationKeepsTheLongestPrefixThatFits() {
        String text = "0123456789";
        for (int keep = 1; keep <= 6; keep++) {
            int maxWidth = (keep + 3) * 6;
            String result = truncate(text, maxWidth);
            assertEquals(text.substring(0, keep) + "...", result,
                    "wrong prefix kept for width " + maxWidth);
        }
    }

    @Test
    void resultMatchesALinearScanForEveryPrefixLength() {
        String text = "TheQuickBrownFoxJumpsOverTheLazyDog";
        for (int maxWidth = 20; maxWidth <= 300; maxWidth++) {
            assertEquals(linearReference(text, maxWidth), truncate(text, maxWidth),
                    "binary search disagrees with a linear scan at width " + maxWidth);
        }
    }

    @Test
    void unicodeTextIsHandledWithoutIndexErrors() {
        String[] samples = {
                "Кириллица",
                "日本語のテキスト",
                "한국어텍스트",
                "TürkçeKarakterler",
                "PolskieZnaki"
        };
        for (String sample : samples) {
            for (int maxWidth = 1; maxWidth <= 200; maxWidth += 3) {
                String result = truncate(sample, maxWidth);
                assertTrue(result != null && !result.isEmpty(), "empty result for " + sample);
            }
        }
    }

    @Test
    void truncationUnderLoadStaysStable() {
        String text = "AVeryLongPlayerNameThatWillDefinitelyNeedTruncating";
        String expected = truncate(text, 120);

        for (int i = 0; i < 50_000; i++) {
            assertEquals(expected, truncate(text, 120));
        }
    }

    private String linearReference(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        int budget = maxWidth - font.width("...");
        if (budget <= 0) return "...";
        int keep = 0;
        for (int i = 1; i <= text.length(); i++) {
            if (font.width(text.substring(0, i)) <= budget) {
                keep = i;
            } else {
                break;
            }
        }
        return text.substring(0, keep) + "...";
    }
}
