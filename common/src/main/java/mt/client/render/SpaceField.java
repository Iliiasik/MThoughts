package mt.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.MTConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

public class SpaceField {
    private static final ResourceLocation[] LARGE_TEXTURES = {
            texture("blue_giant"), texture("orange_dwarf"), texture("quasi_star")
    };
    private static final ResourceLocation[] MEDIUM_TEXTURES = {
            texture("rounded_blue"), texture("rounded_pink"), texture("rounded_white")
    };
    private static final ResourceLocation[] SMALL_TEXTURES = {
            texture("blue"), texture("orange"), texture("white"), texture("yellow"),
            texture("red"), texture("purple"), texture("pink")
    };

    private static final int LARGE_COUNT = 2;
    private static final int MEDIUM_COUNT = 4;
    private static final int SMALL_COUNT = 48;
    private static final int TOTAL_COUNT = LARGE_COUNT + MEDIUM_COUNT + SMALL_COUNT;

    private static final int LARGE_VIRTUAL_SIZE = 48;
    private static final int MEDIUM_VIRTUAL_SIZE = 26;
    private static final int SMALL_VIRTUAL_SIZE = 15;

    private static final float LARGE_PEAK_ALPHA = 0.50f;
    private static final float MEDIUM_PEAK_ALPHA = 0.62f;
    private static final float SMALL_PEAK_ALPHA = 0.75f;

    private static final int LARGE_FADE_MIN_MS = 5800;
    private static final int LARGE_FADE_MAX_MS = 8200;
    private static final int MEDIUM_FADE_MIN_MS = 3600;
    private static final int MEDIUM_FADE_MAX_MS = 5700;
    private static final int SMALL_FADE_MIN_MS = 2100;
    private static final int SMALL_FADE_MAX_MS = 3500;

    private static final float LARGE_SPACING = 1.9f;
    private static final float MEDIUM_SPACING = 2.1f;
    private static final float SMALL_SPACING = 1.8f;

    private static final int CONTENT_MARGIN_VIRTUAL = 28;
    static final int EDGE_MARGIN_VIRTUAL = 40;
    private static final int PLACEMENT_ATTEMPTS = 120;

    private final ResourceLocation[] texture = new ResourceLocation[TOTAL_COUNT];
    private final int[] x = new int[TOTAL_COUNT];
    private final int[] y = new int[TOTAL_COUNT];
    private final int[] size = new int[TOTAL_COUNT];
    private final int[] keepOut = new int[TOTAL_COUNT];
    private final float[] peakAlpha = new float[TOTAL_COUNT];
    private final float[] fadeSpeed = new float[TOTAL_COUNT];
    private final float[] phase = new float[TOTAL_COUNT];
    private final Random rng = new Random();

    private int placed;
    private int layoutWidth = -1;
    private int layoutHeight = -1;
    private long origin = System.currentTimeMillis();
    private boolean dirty = true;

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID,
                "textures/gui/shared/overlay/space/" + name + ".png");
    }

    public void reset(long now) {
        origin = now;
        dirty = true;
    }

    public void layout(int width, int height, float baseScale, List<OverlayRect> content) {
        if (!dirty && width == layoutWidth && height == layoutHeight) return;
        layoutWidth = width;
        layoutHeight = height;
        dirty = false;
        placed = 0;

        int contentMargin = Math.round(CONTENT_MARGIN_VIRTUAL * baseScale);
        int edgeMargin = Math.round(EDGE_MARGIN_VIRTUAL * baseScale);

        place(LARGE_COUNT, LARGE_TEXTURES, LARGE_VIRTUAL_SIZE, LARGE_PEAK_ALPHA,
                LARGE_FADE_MIN_MS, LARGE_FADE_MAX_MS, LARGE_SPACING,
                width, height, baseScale, content, contentMargin, edgeMargin);
        place(MEDIUM_COUNT, MEDIUM_TEXTURES, MEDIUM_VIRTUAL_SIZE, MEDIUM_PEAK_ALPHA,
                MEDIUM_FADE_MIN_MS, MEDIUM_FADE_MAX_MS, MEDIUM_SPACING,
                width, height, baseScale, content, contentMargin, edgeMargin);
        place(SMALL_COUNT, SMALL_TEXTURES, SMALL_VIRTUAL_SIZE, SMALL_PEAK_ALPHA,
                SMALL_FADE_MIN_MS, SMALL_FADE_MAX_MS, SMALL_SPACING,
                width, height, baseScale, content, contentMargin, edgeMargin);
    }

    private void place(int count, ResourceLocation[] textures, int virtualSize, float alpha,
                       int fadeMinMs, int fadeMaxMs, float spacing,
                       int width, int height, float baseScale, List<OverlayRect> content,
                       int contentMargin, int edgeMargin) {
        int drawSize = Math.max(2, Math.round(virtualSize * baseScale));
        int radius = Math.max(1, Math.round(drawSize * spacing / 2.0f));
        int minX = edgeMargin + drawSize / 2;
        int maxX = width - edgeMargin - drawSize / 2;
        int minY = edgeMargin + drawSize / 2;
        int maxY = height - edgeMargin - drawSize / 2;
        if (minX >= maxX || minY >= maxY) return;

        int textureOffset = rng.nextInt(textures.length);
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
                int cx = minX + rng.nextInt(maxX - minX);
                int cy = minY + rng.nextInt(maxY - minY);
                if (hitsContent(content, cx, cy, radius + contentMargin)) continue;
                if (hitsPlaced(cx, cy, radius)) continue;

                int slot = Math.min(textures.length - 1, i * textures.length / count);
                texture[placed] = textures[(slot + textureOffset) % textures.length];
                x[placed] = cx - drawSize / 2;
                y[placed] = cy - drawSize / 2;
                size[placed] = drawSize;
                keepOut[placed] = radius;
                peakAlpha[placed] = alpha;
                fadeSpeed[placed] = (float) (2.0 * Math.PI / (fadeMinMs + rng.nextInt(fadeMaxMs - fadeMinMs)));
                phase[placed] = rng.nextFloat() * 6.2832f;
                placed++;
                break;
            }
        }
    }

    private static boolean hitsContent(List<OverlayRect> content, int cx, int cy, int radius) {
        for (OverlayRect rect : content) {
            if (rect.intersects(cx, cy, radius)) return true;
        }
        return false;
    }

    private boolean hitsPlaced(int cx, int cy, int radius) {
        for (int i = 0; i < placed; i++) {
            int ox = x[i] + size[i] / 2;
            int oy = y[i] + size[i] / 2;
            int dx = cx - ox;
            int dy = cy - oy;
            int reach = radius + keepOut[i];
            if (dx * dx + dy * dy < reach * reach) return true;
        }
        return false;
    }

    public void render(GuiGraphics context) {
        if (placed == 0) return;
        float elapsed = System.currentTimeMillis() - origin;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < placed; i++) {
            float wave = 0.5f + 0.5f * (float) Math.sin(elapsed * fadeSpeed[i] + phase[i]);
            float alpha = peakAlpha[i] * wave;
            if (alpha <= 0.01f) continue;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            context.blit(texture[i], x[i], y[i], size[i], size[i], 0.0f, 0.0f, size[i], size[i], size[i], size[i]);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public boolean occupies(int px, int py) {
        for (int i = 0; i < placed; i++) {
            int ox = x[i] + size[i] / 2;
            int oy = y[i] + size[i] / 2;
            int dx = px - ox;
            int dy = py - oy;
            if (dx * dx + dy * dy < keepOut[i] * keepOut[i]) return true;
        }
        return false;
    }
}
