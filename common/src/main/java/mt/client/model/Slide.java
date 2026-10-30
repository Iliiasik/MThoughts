package mt.client.model;

public record Slide(
        String text,
        SlideCategory category,
        float rarity
) {
    public static Slide of(String text, SlideCategory category) {
        return new Slide(text, category, 1.0f);
    }

    public static Slide ofRare(String text, SlideCategory category, float rarity) {
        return new Slide(text, category, rarity);
    }
}
