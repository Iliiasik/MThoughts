package mt.client.ui.summary;

public record BadgeDimensions(int height, int padding, int spacing, int rowSpacing, float textScale) {

    public static BadgeDimensions calculate(SummaryDimensions dims) {
        if (dims.isCompactMode) {
            return new BadgeDimensions(
                Math.max(8, (int)(8 * dims.uiScale)),
                Math.max(2, (int)(2 * dims.uiScale)),
                Math.max(2, (int)(2 * dims.uiScale)),
                Math.max(1, (int)(1 * dims.uiScale)),
                Math.max(0.5f, dims.uiScale * 0.75f)
            );
        } else {
            return new BadgeDimensions(
                Math.max(12, (int)(14 * dims.uiScale)),
                (int)(5 * dims.uiScale),
                (int)(4 * dims.uiScale),
                (int)(3 * dims.uiScale),
                1.0f
            );
        }
    }
}

