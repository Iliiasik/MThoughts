package mt.client.ui.summary;

public record BadgeDimensions(int height, int padding, int spacing, int rowSpacing, float textScale) {

    public static final BadgeDimensions DEFAULT = new BadgeDimensions(16, 5, 4, 3, 0.9f);

    public static BadgeDimensions calculate(SummaryDimensions dims) {
        int height = dims.s(21);
        int padding = dims.s(7);
        int spacing = dims.s(5);
        int rowSpacing = dims.s(4);
        float textScale = Math.max(0.7f, dims.uiScale * 1.27f);
        return new BadgeDimensions(height, padding, spacing, rowSpacing, textScale);
    }
}
