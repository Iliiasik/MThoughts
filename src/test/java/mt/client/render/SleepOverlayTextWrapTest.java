package mt.client.render;

import net.minecraft.client.gui.Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SleepOverlayTextWrapTest {

    private static final int LATIN_WIDTH = 6;
    private static final int WIDE_WIDTH = 12;

    private Font font;

    @BeforeEach
    void setUp() {
        font = Mockito.mock(Font.class, Mockito.withSettings().stubOnly());
        Mockito.when(font.width(Mockito.anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0, String.class);
            int width = 0;
            for (int i = 0; i < text.length(); i++) {
                width += text.charAt(i) > 0x2E80 ? WIDE_WIDTH : LATIN_WIDTH;
            }
            return width;
        });
    }

    private List<String> wrap(String text, int maxWidth) {
        return SleepOverlayRenderer.wrapText(text, font, maxWidth);
    }

    private void assertEveryLineFits(List<String> lines, int maxWidth) {
        for (String line : lines) {
            assertTrue(font.width(line) <= maxWidth,
                    "line \"" + line + "\" is " + font.width(line) + " wide, limit is " + maxWidth);
        }
    }

    @Test
    void nullAndEmptyTextProduceNoLines() {
        assertTrue(wrap(null, 100).isEmpty());
        assertTrue(wrap("", 100).isEmpty());
    }

    @Test
    void shortLatinTextStaysOnOneLine() {
        assertEquals(List.of("Cats sleep a lot"), wrap("Cats sleep a lot", 600));
    }

    @Test
    void latinTextWrapsOnSpaces() {
        List<String> lines = wrap("one two three four five six seven eight", 60);

        assertTrue(lines.size() > 1);
        assertEveryLineFits(lines, 60);
        assertEquals("one two three four five six seven eight", String.join(" ", lines));
    }

    @Test
    void chineseTextIsBrokenIntoSeveralLines() {
        String text = "这是一个非常长的句子它没有任何空格所以必须按字符换行否则会超出屏幕";

        List<String> lines = wrap(text, 120);

        assertTrue(lines.size() > 1,
                "text without spaces must not stay on a single line");
        assertEveryLineFits(lines, 120);
        assertEquals(text, String.join("", lines));
    }

    @Test
    void japaneseTextIsBrokenIntoSeveralLines() {
        String text = "これはとても長い文章でスペースがないため文字単位で折り返す必要があります";

        List<String> lines = wrap(text, 120);

        assertTrue(lines.size() > 1);
        assertEveryLineFits(lines, 120);
        assertEquals(text, String.join("", lines));
    }

    @Test
    void koreanTextIsBrokenIntoSeveralLines() {
        String text = "이것은매우긴문장이며공백이없으므로문자단위로줄바꿈해야합니다";

        List<String> lines = wrap(text, 120);

        assertTrue(lines.size() > 1);
        assertEveryLineFits(lines, 120);
        assertEquals(text, String.join("", lines));
    }

    @Test
    void aSingleOverlongLatinWordIsBrokenToo() {
        String word = "a".repeat(200);

        List<String> lines = wrap(word, 60);

        assertTrue(lines.size() > 1);
        assertEveryLineFits(lines, 60);
        assertEquals(word, String.join("", lines));
    }

    @Test
    void anOverlongWordDoesNotSwallowTheWordsAroundIt() {
        List<String> lines = wrap("before " + "x".repeat(60) + " after", 60);

        assertEveryLineFits(lines, 60);
        assertEquals("before", lines.get(0));
        assertTrue(String.join("", lines).endsWith("after"));
    }

    @Test
    void mixedScriptTextStaysWithinTheWidth() {
        List<String> lines = wrap("Cats 猫は一日の七十パーセントを眠る and that is a fact", 120);

        assertFalse(lines.isEmpty());
        assertEveryLineFits(lines, 120);
    }

    @Test
    void everyLineFitsForEveryWidth() {
        String[] samples = {
                "The quick brown fox jumps over the lazy dog",
                "这是一个非常长的句子它没有任何空格",
                "これはとても長い文章です",
                "Кошки спят семьдесят процентов своей жизни",
                "Süpermarkette çok uzun bir cümle yazıyorum"
        };

        for (String sample : samples) {
            for (int maxWidth = WIDE_WIDTH; maxWidth <= 300; maxWidth += 7) {
                assertEveryLineFits(wrap(sample, maxWidth), maxWidth);
            }
        }
    }

    @Test
    void noLineIsEmpty() {
        String[] samples = {
                "one two three",
                "这是一个非常长的句子",
                "x".repeat(100),
                "a  b   c"
        };

        for (String sample : samples) {
            for (String line : wrap(sample, 60)) {
                assertFalse(line.isEmpty(), "empty line produced for \"" + sample + "\"");
            }
        }
    }

    @Test
    void surrogatePairsAreNeverSplit() {
        String text = "𠜎𠜱𠝹𠱓𠱸𠲖𠳏𠳕𠴕𠵼𠵿𠸎𠸏𠹷𠺝𠺢";

        List<String> lines = wrap(text, 60);

        assertEquals(text, String.join("", lines));
        for (String line : lines) {
            assertFalse(Character.isHighSurrogate(line.charAt(line.length() - 1)),
                    "a surrogate pair was split across lines");
            assertFalse(Character.isLowSurrogate(line.charAt(0)),
                    "a surrogate pair was split across lines");
        }
    }

    @Test
    void wrappingIsDeterministic() {
        String text = "这是一个非常长的句子它没有任何空格所以必须按字符换行";

        List<String> first = wrap(text, 100);
        for (int i = 0; i < 1000; i++) {
            assertEquals(first, wrap(text, 100));
        }
    }
}
