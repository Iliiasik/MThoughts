package mt.client.ui.summary;

public record BadgeDimensions(int height, int padding, int spacing, int rowSpacing, float textScale) {

    public static final BadgeDimensions DEFAULT = new BadgeDimensions(16, 5, 4, 3, 0.9f);

    public static BadgeDimensions calculate(SummaryDimensions dims) {
        int height = (int)(16 * dims.uiScale);
        int padding = (int)(5 * dims.uiScale);
        int spacing = (int)(4 * dims.uiScale);
        int rowSpacing = (int)(3 * dims.uiScale);
        float textScale = Math.max(0.7f, dims.uiScale * 0.95f);
        return new BadgeDimensions(height, padding, spacing, rowSpacing, textScale);
    }
}
