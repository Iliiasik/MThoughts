package mt.client.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlideCollectionTest {

    @Test
    void testEmptyCollection() {
        SlideCollection collection = new SlideCollection(List.of());

        assertNotNull(collection.entries());
        assertTrue(collection.entries().isEmpty());
    }

    @Test
    void testSingleEntry() {
        SlideCollection.SlideEntry entry = new SlideCollection.SlideEntry("Test text", 1.0f);
        SlideCollection collection = new SlideCollection(List.of(entry));

        assertEquals(1, collection.entries().size());
        assertEquals("Test text", collection.entries().get(0).text());
        assertEquals(1.0f, collection.entries().get(0).rarity());
    }

    @Test
    void testMultipleEntries() {
        List<SlideCollection.SlideEntry> entries = List.of(
            new SlideCollection.SlideEntry("Entry 1", 1.0f),
            new SlideCollection.SlideEntry("Entry 2", 0.5f),
            new SlideCollection.SlideEntry("Entry 3", 0.1f)
        );

        SlideCollection collection = new SlideCollection(entries);

        assertEquals(3, collection.entries().size());
        assertEquals("Entry 1", collection.entries().get(0).text());
        assertEquals("Entry 2", collection.entries().get(1).text());
        assertEquals("Entry 3", collection.entries().get(2).text());
    }

    @Test
    void testDefaultRarity() {
        SlideCollection.SlideEntry entry = new SlideCollection.SlideEntry("Test");

        assertEquals("Test", entry.text());
        assertEquals(1.0f, entry.rarity());
    }

    @Test
    void testCustomRarity() {
        SlideCollection.SlideEntry entry = new SlideCollection.SlideEntry("Rare", 0.1f);

        assertEquals("Rare", entry.text());
        assertEquals(0.1f, entry.rarity());
    }

    @Test
    void testEntryEquality() {
        SlideCollection.SlideEntry entry1 = new SlideCollection.SlideEntry("Test", 1.0f);
        SlideCollection.SlideEntry entry2 = new SlideCollection.SlideEntry("Test", 1.0f);

        assertEquals(entry1, entry2);
    }

    @Test
    void testCollectionEquality() {
        List<SlideCollection.SlideEntry> entries1 = List.of(
            new SlideCollection.SlideEntry("Entry 1")
        );
        List<SlideCollection.SlideEntry> entries2 = List.of(
            new SlideCollection.SlideEntry("Entry 1")
        );

        SlideCollection collection1 = new SlideCollection(entries1);
        SlideCollection collection2 = new SlideCollection(entries2);

        assertEquals(collection1, collection2);
    }

    @Test
    void testLargeCollection() {
        List<SlideCollection.SlideEntry> entries = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            entries.add(new SlideCollection.SlideEntry("Entry " + i, (float)Math.random()));
        }

        SlideCollection collection = new SlideCollection(entries);

        assertEquals(1000, collection.entries().size());
    }

    @Test
    void testMixedRarities() {
        List<SlideCollection.SlideEntry> entries = List.of(
            new SlideCollection.SlideEntry("Common", 1.0f),
            new SlideCollection.SlideEntry("Uncommon", 0.7f),
            new SlideCollection.SlideEntry("Rare", 0.3f),
            new SlideCollection.SlideEntry("Epic", 0.1f),
            new SlideCollection.SlideEntry("Legendary", 0.01f)
        );

        SlideCollection collection = new SlideCollection(entries);

        assertEquals(5, collection.entries().size());
        assertTrue(collection.entries().get(0).rarity() > collection.entries().get(1).rarity());
        assertTrue(collection.entries().get(1).rarity() > collection.entries().get(2).rarity());
    }

    @Test
    void testNullEntriesHandling() {
        SlideCollection collection = new SlideCollection(null);

        assertNull(collection.entries());
    }

    @Test
    void testImmutability() {
        List<SlideCollection.SlideEntry> entries = List.of(
            new SlideCollection.SlideEntry("Entry 1")
        );

        SlideCollection collection = new SlideCollection(entries);

        assertThrows(UnsupportedOperationException.class, () -> {
            collection.entries().add(new SlideCollection.SlideEntry("Entry 2"));
        });
    }
}

