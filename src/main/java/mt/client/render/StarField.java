package mt.client.render;

import net.minecraft.client.gui.GuiGraphics;

import java.util.Random;

public class StarField {
    private static final int STAR_COUNT = 70;
    private static final int SHUFFLE_MIN_MS = 700;
    private static final int SHUFFLE_MAX_MS = 2200;

    private final float[] x = new float[STAR_COUNT];
    private final float[] y = new float[STAR_COUNT];
    private final float[] alpha = new float[STAR_COUNT];
    private final float[] speed = new float[STAR_COUNT];
    private final float[] phase = new float[STAR_COUNT];
    private final float[] drift = new float[STAR_COUNT];
    private final boolean[] big = new boolean[STAR_COUNT];
    private final Random rng = new Random();
    private long nextShuffle;

    public StarField() {
        regenerate();
    }

    public void reset(long now) {
        regenerate();
        scheduleShuffle(now);
    }

    public void tick(long now) {
        if (now >= nextShuffle) {
            respawn(rng.nextInt(STAR_COUNT));
            respawn(rng.nextInt(STAR_COUNT));
            scheduleShuffle(now);
        }
    }

    public void render(GuiGraphics context, int w, int h, float overlayAlpha) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < STAR_COUNT; i++) {
            float twinkle = 0.35f + 0.65f * (float) Math.sin(now * speed[i] + phase[i]);
            int a = clampAlpha(alpha[i] * twinkle * overlayAlpha);
            if (a <= 1) continue;
            float dy = y[i] + now * drift[i];
            dy = dy - (float) Math.floor(dy);
            int px = Math.round(x[i] * w);
            int py = Math.round(dy * h);
            int size = big[i] ? 2 : 1;
            context.fill(px, py, px + size, py + size, (a << 24) | 0xFFFFFF);
        }
    }

    private void regenerate() {
        for (int i = 0; i < STAR_COUNT; i++) respawn(i);
    }

    private void respawn(int i) {
        x[i] = rng.nextFloat();
        y[i] = rng.nextFloat();
        alpha[i] = 0.25f + rng.nextFloat() * 0.45f;
        speed[i] = 0.0015f + rng.nextFloat() * 0.006f;
        phase[i] = rng.nextFloat() * 6.2832f;
        drift[i] = 0.5e-6f + rng.nextFloat() * 2.5e-6f;
        big[i] = rng.nextFloat() < 0.30f;
    }

    private void scheduleShuffle(long now) {
        nextShuffle = now + SHUFFLE_MIN_MS + rng.nextInt(SHUFFLE_MAX_MS - SHUFFLE_MIN_MS);
    }

    private int clampAlpha(float a) {
        int v = Math.round(a * 255f);
        return v < 0 ? 0 : Math.min(v, 255);
    }
}