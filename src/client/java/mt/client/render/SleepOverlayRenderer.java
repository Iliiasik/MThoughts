package mt.client.render;

import mt.client.MidnightThoughtsClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.manager.WellRestedClientState;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.service.SlideService;
import mt.network.packet.RequestMoonPhasePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SleepOverlayRenderer {
    private static final Identifier SKULL_TEXTURE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/skull.png");

    private static final Identifier[] MOON_PHASE_TEXTURES = {
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/full_moon.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waning_gibbous.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/last_quarter.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waning_crescent.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/new_moon.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waxing_crescent.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/first_quarter.png"),
            Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waxing_gibbous.png")
    };

    private static final int IMAGE_ORIGINAL_WIDTH = 360;
    private static final int IMAGE_ORIGINAL_HEIGHT = 360;
    private static final float IMAGE_SCALE_PERCENT = 0.35f;
    private static final float GAP_PERCENT = 0.03f;
    private static final float TEXT_AREA_WIDTH_PERCENT = 0.35f;
    private static final float MIN_TEXT_SCALE = 1.5f;
    private static final float MAX_TEXT_SCALE = 3.0f;
    private static final float ALPHA_LERP_SPEED = 0.15f;
    private static final long SLEEP_DEBOUNCE_MS = 200;

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
    private int moonPhase = 0;
    private boolean isNightmareOverlay = false;

    private enum SlideState {
        HIDDEN, FADING_IN, VISIBLE, FADING_OUT
    }

    public SleepOverlayRenderer(SleepStateManager sleepStateManager, SlideService slideService, MidnightThoughtsConfig config) {
        this.sleepStateManager = sleepStateManager;
        this.slideService = slideService;
        this.config = config;
    }

    public void setMoonPhase(int moonPhase) {
        this.moonPhase = moonPhase;
    }

    public void tick() {
        if (config.isEnableOverlay()) return;

        long now = System.currentTimeMillis();

        if (sleepStateManager.justStoppedSleeping()) {
            lastSleepEndTime = now;
        }

        if (sleepStateManager.justStartedSleeping()) {
            boolean wasRecentlySleeping = (now - lastSleepEndTime) < SLEEP_DEBOUNCE_MS;
            if (!isOverlayVisible && !wasRecentlySleeping) {
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
        ClientPlayNetworking.send(new RequestMoonPhasePacket());
        isNightmareOverlay = WellRestedClientState.isNightmareMode();
        isOverlayVisible = true;
        overlayAlpha = 1f;
        textAlpha = 1f;
        targetTextAlpha = 1f;
        currentSlide = getNightmareAwareSlide();
        nextSlide = getNightmareAwareSlide();
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
        isNightmareOverlay = false;
    }

    private Slide getNightmareAwareSlide() {
        if (WellRestedClientState.isNightmareMode()) {
            Slide s = slideService.getSlideByCategory(slideService.getCurrentLanguage(), SlideCategory.NIGHTMARE);
            if (s != null) return s;
        }
        return slideService.getNextSlide();
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
                    nextSlide = getNightmareAwareSlide();
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
                    currentSlide = nextSlide != null ? nextSlide : getNightmareAwareSlide();
                    nextSlide = null;
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
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    public void renderOverlayOnly(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || config.isEnableOverlay()) return;
        if (overlayAlpha <= 0 && !isOverlayVisible) return;
        renderOverlay(context, screenWidth, screenHeight);
    }

    public void renderContentOnly(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || config.isEnableOverlay()) return;
        if (overlayAlpha <= 0 && !isOverlayVisible) return;
        renderContent(context, screenWidth, screenHeight);
    }

    private void renderOverlay(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        int alpha = (int) (config.getOverlayOpacity() * overlayAlpha * 255);
        int overlayColor = isNightmareOverlay
                ? (alpha << 24) | 0x1A0000
                : (alpha << 24);
        context.fill(0, 0, screenWidth, screenHeight, overlayColor);
    }

    private void renderContent(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

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

        if (currentSlide != null && textAlpha > 0.01f) {
            int scaledTextWidth = (int) (textAreaWidth / textScale);
            List<String> lines = wrapText(currentSlide.text(), font, scaledTextWidth);
            renderSlideText(context, font, lines, textAreaX, centerY, textScale);
        }
    }

    private float calculateTextScale(int screenHeight) {
        float baseScale = screenHeight / 400f;
        return Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, baseScale));
    }

    private void renderImage(GuiGraphicsExtractor context, int x, int y, int width, int height) {
        Identifier texture = isNightmareOverlay
                ? SKULL_TEXTURE
                : MOON_PHASE_TEXTURES[moonPhase % MOON_PHASE_TEXTURES.length];

        int alpha = (int) (overlayAlpha * config.getImageOpacity() * 255);
        int color = (alpha << 24) | 0xFFFFFF;

        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(width / (float) IMAGE_ORIGINAL_WIDTH, height / (float) IMAGE_ORIGINAL_HEIGHT);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f,
                IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT,
                IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT,
                IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT, color);
        context.pose().popMatrix();
    }

    private void renderSlideText(GuiGraphicsExtractor context, Font font, List<String> lines, int areaX, int centerY, float scale) {
        int alpha = (int) (config.getTextOpacity() * textAlpha * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        int lineHeight = font.lineHeight + 4;
        int totalTextHeight = (int) (lines.size() * lineHeight * scale);
        int textY = centerY - totalTextHeight / 2;

        context.pose().pushMatrix();
        context.pose().translate(areaX, textY);
        context.pose().scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            context.text(font, lines.get(i), 0, i * lineHeight, textColor, true);
        }

        context.pose().popMatrix();
    }

    private List<String> wrapText(String text, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(" ");
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
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    public boolean shouldHideCrosshair() {
        return sleepStateManager.isSleeping() && isOverlayVisible;
    }

    public boolean shouldHideChat() {
        return config.isHideChatWhenSleeping() && sleepStateManager.isSleeping() && isOverlayVisible;
    }
}