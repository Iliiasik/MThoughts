package mt.client.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlideTest {

    @Test
    void testSlideCreation() {
        Slide slide = Slide.of("Test text", SlideCategory.FACT);

        assertEquals("Test text", slide.text());
        assertEquals(SlideCategory.FACT, slide.category());
        assertEquals(1.0f, slide.rarity());
    }

    @Test
    void testRareSlideCreation() {
        Slide slide = Slide.ofRare("Rare text", SlideCategory.SPECIAL, 0.1f);

        assertEquals("Rare text", slide.text());
        assertEquals(SlideCategory.SPECIAL, slide.category());
        assertEquals(0.1f, slide.rarity());
    }

    @Test
    void testSlideEquality() {
        Slide slide1 = Slide.of("Test", SlideCategory.FACT);
        Slide slide2 = Slide.of("Test", SlideCategory.FACT);

        assertEquals(slide1, slide2);
    }

    @Test
    void testSlideInequality() {
        Slide slide1 = Slide.of("Test1", SlideCategory.FACT);
        Slide slide2 = Slide.of("Test2", SlideCategory.FACT);

        assertNotEquals(slide1, slide2);
    }

    @Test
    void testAllCategories() {
        Slide fact = Slide.of("Fact", SlideCategory.FACT);
        Slide lore = Slide.of("Lore", SlideCategory.LORE);
        Slide surreal = Slide.of("Surreal", SlideCategory.SURREAL);
        Slide special = Slide.of("Special", SlideCategory.SPECIAL);

        assertEquals(SlideCategory.FACT, fact.category());
        assertEquals(SlideCategory.LORE, lore.category());
        assertEquals(SlideCategory.SURREAL, surreal.category());
        assertEquals(SlideCategory.SPECIAL, special.category());
    }

    @Test
    void testEmptyText() {
        Slide slide = Slide.of("", SlideCategory.FACT);

        assertEquals("", slide.text());
        assertNotNull(slide.text());
    }

    @Test
    void testLongText() {
        String longText = "A".repeat(1000);
        Slide slide = Slide.of(longText, SlideCategory.LORE);

        assertEquals(1000, slide.text().length());
    }

    @Test
    void testRarityRange() {
        Slide common = Slide.ofRare("Common", SlideCategory.FACT, 1.0f);
        Slide uncommon = Slide.ofRare("Uncommon", SlideCategory.FACT, 0.5f);
        Slide rare = Slide.ofRare("Rare", SlideCategory.SPECIAL, 0.1f);
        Slide legendary = Slide.ofRare("Legendary", SlideCategory.SPECIAL, 0.01f);

        assertTrue(common.rarity() > uncommon.rarity());
        assertTrue(uncommon.rarity() > rare.rarity());
        assertTrue(rare.rarity() > legendary.rarity());
    }

    @Test
    void testMultilineText() {
        String multiline = "Line 1\nLine 2\nLine 3";
        Slide slide = Slide.of(multiline, SlideCategory.LORE);

        assertEquals(multiline, slide.text());
        assertTrue(slide.text().contains("\n"));
    }

    @Test
    void testSpecialCharacters() {
        String special = "Special: !@#$%^&*()_+-=[]{}|;:',.<>?/";
        Slide slide = Slide.of(special, SlideCategory.FACT);

        assertEquals(special, slide.text());
    }

    @Test
    void testUnicodeText() {
        String unicode = "Unicode: 你好世界 🌍 Привет мир";
        Slide slide = Slide.of(unicode, SlideCategory.FACT);

        assertEquals(unicode, slide.text());
    }
}

