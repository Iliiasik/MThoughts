package mt.client.render;

import mt.client.MidnightThoughtsClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.model.Slide;
import mt.client.service.SlideService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

public class SleepOverlayRenderer {
    private static final Identifier IMAGE_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/moon.png");

    private static final int IMAGE_ORIGINAL_WIDTH = 421;
    private static final int IMAGE_ORIGINAL_HEIGHT = 407;

    private static final float IMAGE_SCALE_PERCENT = 0.35f;
    private static final float GAP_PERCENT = 0.03f;
    private static final float TEXT_AREA_WIDTH_PERCENT = 0.35f;
    private static final float MIN_TEXT_SCALE = 1.5f;
    private static final float MAX_TEXT_SCALE = 3.0f;
    private static final float IMAGE_OPACITY = 0.2f;

    private final SleepStateManager sleepStateManager;
    private final SlideService slideService;
    private final MidnightThoughtsConfig config;

    private Slide currentSlide;
    private Slide nextSlide;
    private long slideStartTime;
    private long visibleStartTime;
    private long lastSleepEndTime;
    private int currentSlideDuration;
    private float textAlpha = 0f;
    private float targetTextAlpha = 0f;
    private float overlayAlpha = 0f;
    private SlideState slideState = SlideState.HIDDEN;
    private boolean isOverlayVisible = false;
    private static final float ALPHA_LERP_SPEED = 0.15f;
    private static final long SLEEP_DEBOUNCE_MS = 200;

    private enum SlideState {
        HIDDEN,
        FADING_IN,
        VISIBLE,
        FADING_OUT
    }

    public SleepOverlayRenderer(SleepStateManager sleepStateManager, SlideService slideService, MidnightThoughtsConfig config) {
        this.sleepStateManager = sleepStateManager;
        this.slideService = slideService;
        this.config = config;
    }

    public void tick() {
        if (!config.isEnableOverlay()) {
            return;
        }

        long now = System.currentTimeMillis();

        if (sleepStateManager.justStoppedSleeping()) {
            lastSleepEndTime = now;
        }

        if (sleepStateManager.justStartedSleeping()) {
            boolean wasRecentlySleeping = (now - lastSleepEndTime) < SLEEP_DEBOUNCE_MS;
            if (!isOverlayVisible &&!wasRecentlySleeping) {
                onSleepStart();
            }
        }

        if (!sleepStateManager.isSleeping() && isOverlayVisible) {
            boolean debounceExpired = (now - lastSleepEndTime) >= SLEEP_DEBOUNCE_MS;
            if (debounceExpired) {
                onSleepEnd();
            }
        }

        if (sleepStateManager.isSleeping() && isOverlayVisible) {
            updateOverlayAlpha();
            updateSlideState();
        }
    }

    private void onSleepStart() {
        slideService.refreshStats();
        slideService.resetSlideCounter();
        isOverlayVisible = true;
        overlayAlpha = 1f;
        textAlpha = 1f;
        targetTextAlpha = 1f;
        currentSlide = slideService.getNextSlide();
        nextSlide = slideService.getNextSlide();
        currentSlideDuration = config.getRandomSlideDisplayTime();
        slideStartTime = System.currentTimeMillis();
        visibleStartTime = System.currentTimeMillis();
        slideState = SlideState.VISIBLE;
    }

    private void onSleepEnd() {
        currentSlide = null;
        nextSlide = null;
        slideState = SlideState.HIDDEN;
        textAlpha = 0f;
        targetTextAlpha = 0f;
        overlayAlpha = 0f;
        isOverlayVisible = false;
    }

    private void updateOverlayAlpha() {
        if (isOverlayVisible && overlayAlpha < 1f) {
            overlayAlpha = Math.min(1f, overlayAlpha + 0.02f);
        }
    }

    private void updateSlideState() {
        long now = System.currentTimeMillis();

        switch (slideState) {
            case FADING_IN -> {
                long elapsedFadeIn = now - slideStartTime;
                float progressFadeIn = (float) elapsedFadeIn / config.getFadeInDurationMs();

                if (progressFadeIn >= 1f && textAlpha > 0.95f) {
                    targetTextAlpha = 1f;
                    slideState = SlideState.VISIBLE;
                    visibleStartTime = now;
                    nextSlide = slideService.getNextSlide();
                } else {
                    targetTextAlpha = easeInOut(Math.min(progressFadeIn, 1f));
                }
            }
            case VISIBLE -> {
                targetTextAlpha = 1f;
                long elapsedVisible = now - visibleStartTime;

                if (elapsedVisible >= currentSlideDuration) {
                    slideState = SlideState.FADING_OUT;
                    slideStartTime = now;
                }
            }
            case FADING_OUT -> {
                long elapsedFadeOut = now - slideStartTime;
                float progressFadeOut = (float) elapsedFadeOut / config.getFadeOutDurationMs();
                targetTextAlpha = 1f - easeInOut(Math.min(progressFadeOut, 1f));

                if (progressFadeOut >= 1f && textAlpha < 0.05f) {
                    if (nextSlide!= null) {
                        currentSlide = nextSlide;
                        nextSlide = null;
                    } else {
                        currentSlide = slideService.getNextSlide();
                    }
                    currentSlideDuration = config.getRandomSlideDisplayTime();
                    slideStartTime = now;
                    targetTextAlpha = 0f;
                    slideState = SlideState.FADING_IN;
                }
            }
            case HIDDEN -> targetTextAlpha = 0f;
        }

        textAlpha = lerp(textAlpha, targetTextAlpha);
        if (Math.abs(textAlpha - targetTextAlpha) < 0.01f) {
            textAlpha = targetTextAlpha;
        }
    }

    private float lerp(float current, float target) {
        return current + (target - current) * ALPHA_LERP_SPEED;
    }

    private float easeInOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t < 0.5f? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() ||!config.isEnableOverlay()) {
            return;
        }

        if (overlayAlpha <= 0 &&!isOverlayVisible) {
            return;
        }

        renderOverlay(context, screenWidth, screenHeight);
        renderContent(context, screenWidth, screenHeight);
    }

    private void renderOverlay(DrawContext context, int screenWidth, int screenHeight) {
        int alpha = (int) (config.getOverlayOpacity() * overlayAlpha * 255);
        int overlayColor = (alpha << 24);
        context.fill(0, 0, screenWidth, screenHeight, overlayColor);
    }

    private void renderContent(DrawContext context, int screenWidth, int screenHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        float imageScale = (screenHeight * IMAGE_SCALE_PERCENT) / IMAGE_ORIGINAL_HEIGHT;
        int imageWidth = (int) (IMAGE_ORIGINAL_WIDTH * imageScale);
        int imageHeight = (int) (IMAGE_ORIGINAL_HEIGHT * imageScale);

        int gap = (int) (screenWidth * GAP_PERCENT);
        int textAreaWidth = (int) (screenWidth * TEXT_AREA_WIDTH_PERCENT);

        float textScale = calculateTextScale(screenHeight);

        int totalContentWidth = imageWidth + gap + textAreaWidth;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int imageX = centerX - totalContentWidth / 2;
        int imageY = centerY - imageHeight / 2;

        int textAreaX = imageX + imageWidth + gap;

        if (config.isEnableImage()) {
            renderImage(context, imageX, imageY, imageWidth, imageHeight);
        }

        if (currentSlide!= null && textAlpha > 0.01f) {
            int scaledTextWidth = (int) (textAreaWidth / textScale);
            List<String> lines = wrapText(currentSlide.text(), textRenderer, scaledTextWidth);
            renderSlideText(context, textRenderer, lines, textAreaX, centerY, textScale);
        }
    }

    private float calculateTextScale(int screenHeight) {
        float baseScale = screenHeight / 400f;
        return Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, baseScale));
    }

    private void renderImage(DrawContext context, int x, int y, int width, int height) {
        int alpha = (int) (overlayAlpha * IMAGE_OPACITY * 255);
        int color = (alpha << 24) | 0xFFFFFF;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                IMAGE_TEXTURE,
                x, y,
                0.0f, 0.0f,
                width, height,
                IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT,
                IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT,
                color
        );
    }

    private void renderSlideText(DrawContext context, TextRenderer textRenderer, List<String> lines, int areaX, int centerY, float scale) {
        int alpha = (int) (config.getTextOpacity() * textAlpha * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        int lineHeight = textRenderer.fontHeight + 4;
        int totalTextHeight = (int) (lines.size() * lineHeight * scale);

        int textY = centerY - totalTextHeight / 2;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(areaX, textY);
        matrices.scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int y = i * lineHeight;
            context.drawTextWithShadow(textRenderer, line, 0, y, textColor);
        }

        matrices.popMatrix();
    }

    private List<String> wrapText(String text, TextRenderer textRenderer, int maxWidth) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty()? word : currentLine + " " + word;

            if (textRenderer.getWidth(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    public boolean shouldHideCrosshair() {
        return sleepStateManager.isSleeping() && isOverlayVisible;
    }
}