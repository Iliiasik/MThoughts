package mt.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NightmareField {
    private static final int WASH_RGB = 0x3A0000;
    private static final int EDGE_RGB = 0x220000;

    private static final float WASH_ALPHA = 0.18f;
    private static final float EDGE_ALPHA = 0.55f;

    private static final float VERTICAL_PERIOD_MS = 5200f;
    private static final float HORIZONTAL_PERIOD_MS = 6700f;
    private static final float HORIZONTAL_PHASE = 0.35f;

    private static final float EDGE_HEIGHT_MIN = 0.24f;
    private static final float EDGE_HEIGHT_MAX = 0.44f;
    private static final float EDGE_WIDTH_MIN = 0.16f;
    private static final float EDGE_WIDTH_MAX = 0.34f;

    private long origin = System.currentTimeMillis();

    public void reset(long now) {
        origin = now;
    }

    public void render(GuiGraphicsExtractor context, int w, int h, float overlayAlpha) {
        if (overlayAlpha <= 0f) return;
        float elapsed = Math.max(0f, System.currentTimeMillis() - origin);

        int wash = clampAlpha(WASH_ALPHA * overlayAlpha);
        if (wash > 2) {
            context.fill(0, 0, w, h, (wash << 24) | WASH_RGB);
        }

        int edge = clampAlpha(EDGE_ALPHA * overlayAlpha);
        if (edge <= 2) return;

        int bandHeight = Math.round(h * lerp(EDGE_HEIGHT_MIN, EDGE_HEIGHT_MAX, verticalClosure(elapsed)));
        int bandWidth = Math.round(w * lerp(EDGE_WIDTH_MIN, EDGE_WIDTH_MAX, horizontalClosure(elapsed)));
        int solid = (edge << 24) | EDGE_RGB;

        context.fillGradient(0, 0, w, bandHeight, solid, EDGE_RGB);
        context.fillGradient(0, h - bandHeight, w, h, EDGE_RGB, solid);
        SleepOverlayRenderer.fillHorizontalGradient(context, 0, bandWidth, h, edge, 0, EDGE_RGB);
        SleepOverlayRenderer.fillHorizontalGradient(context, w - bandWidth, w, h, 0, edge, EDGE_RGB);
    }

    static float verticalClosure(float elapsed) {
        return wave(elapsed, VERTICAL_PERIOD_MS, 0f);
    }

    static float horizontalClosure(float elapsed) {
        return wave(elapsed, HORIZONTAL_PERIOD_MS, HORIZONTAL_PHASE);
    }

    static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static float wave(float elapsed, float periodMs, float phase) {
        double angle = elapsed * (2.0 * Math.PI / periodMs) + phase * 2.0 * Math.PI;
        return 0.5f + 0.5f * (float) Math.sin(angle);
    }

    private static int clampAlpha(float a) {
        if (Float.isNaN(a)) return 0;
        int v = Math.round(a * 255f);
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
