package mt.client.model;

import java.util.List;

public record SlideCollection(
    List<SlideEntry> entries
) {
    public record SlideEntry(
        String text,
        float rarity
    ) {
        public SlideEntry(String text) {
            this(text, 1.0f);
        }
    }
}

