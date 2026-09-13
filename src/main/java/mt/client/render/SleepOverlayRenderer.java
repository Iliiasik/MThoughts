package mt.client.render;

import mt.client.MidnightThoughtsClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.manager.WellRestedClientState;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.service.SlideService;
import mt.network.packet.RequestMoonPhasePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SleepOverlayRenderer {
    private static final ResourceLocation SKULL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/skull.png");

    private static final ResourceLocation[] MOON_PHASE_TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/full_moon.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waning_gibbous.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/last_quarter.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waning_crescent.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/new_moon.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waxing_crescent.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/first_quarter.png"),
            ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/overlay/moon_phases/waxing_gibbous.png")
    };

    private static final float BASE_W = 1920.0f;
    private static final float BASE_H = 1080.0f;
    private static final int VIRTUAL_IMAGE_SIZE = 252;
    private static final int VIRTUAL_GAP = 56;
    private static final int VIRTUAL_TEXT_WIDTH = 1040;
    private static final int VIRTUAL_TEXT_BLOCK = 200;
    private static final float VIRTUAL_TEXT_SCALE = 3.4f;
    private static final int VIRTUAL_LINE_SPACING = 6;
    private static final int VIRTUAL_PROGRESS_WIDTH = 220;
    private static final float GROUP_TOP_PERCENT = 0.30f;
    private static final int MAX_TEXT_LINES = 6;
    private static final int SHORT_TEXT_LINES = 2;
    private static final long SLEEP_DEBOUNCE_MS = 200;

    private final SleepStateManager sleepStateManager;
    private final SlideService slideService;
    private final MidnightThoughtsConfig config;
    private final StarField starField = new StarField();
    private final NightmareField nightmareField = new NightmareField();

    private Slide currentSlide;
    private Slide nextSlide;
    private Slide outgoingSlide;
    private long visibleStartTime;
    private long transitionStart;
    private long lastSleepEndTime;
    private int currentSlideDuration;
    private float incomingAlpha = 0f;
    private float outgoingAlpha = 0f;
    private float overlayAlpha = 0f;
    private SlideState slideState = SlideState.HIDDEN;
    private boolean isOverlayVisible = false;
    private int moonPhase = 0;
    private boolean isNightmareOverlay = false;

    private final LayoutCache currentCache = new LayoutCache();
    private final LayoutCache outgoingCache = new LayoutCache();

    private enum SlideState {
        HIDDEN, VISIBLE, TRANSITION
    }

    private record TextLayout(List<String> lines, float scale) {}

    private static final class LayoutCache {
        private Slide slide;
        private int width = -1;
        private float scale = Float.NaN;
        private TextLayout layout;

        TextLayout get(Slide source, Font font, int textAreaWidth, float uiScale) {
            if (source != slide || textAreaWidth != width || uiScale != scale) {
                slide = source;
                width = textAreaWidth;
                scale = uiScale;
                layout = layoutText(source.text(), font, textAreaWidth, uiScale);
            }
            return layout;
        }
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
        if (!config.isEnableOverlay()) return;

        long now = System.currentTimeMillis();

        if (sleepStateManager.justStoppedSleeping()) lastSleepEndTime = now;

        if (sleepStateManager.justStartedSleeping()) {
            boolean wasRecentlySleeping = (now - lastSleepEndTime) < SLEEP_DEBOUNCE_MS;
            if (!isOverlayVisible && !wasRecentlySleeping) onSleepStart();
        }

        if (!sleepStateManager.isSleeping() && isOverlayVisible) {
            if ((now - lastSleepEndTime) >= SLEEP_DEBOUNCE_MS) onSleepEnd();
        }

        if (sleepStateManager.isSleeping() && isOverlayVisible) {
            syncNightmareState(now);
            updateOverlayAlpha();
            updateSlideState();
            if (config.isEnableStarDust() && !isNightmareOverlay) {
                starField.tick(now);
            }
        }
    }

    private void syncNightmareState(long now) {
        boolean nightmare = WellRestedClientState.isNightmareMode();
        if (nightmare == isNightmareOverlay) return;

        isNightmareOverlay = nightmare;
        currentSlide = getNightmareAwareSlide();
        nextSlide = getNightmareAwareSlide();
        outgoingSlide = null;
        outgoingAlpha = 0f;
        incomingAlpha = 1f;
        currentSlideDuration = config.getRandomSlideDisplayTime();
        visibleStartTime = now;
        slideState = SlideState.VISIBLE;

        if (nightmare) {
            nightmareField.reset(now);
        } else {
            starField.reset(now);
        }
    }

    private void onSleepStart() {
        PacketDistributor.sendToServer(new RequestMoonPhasePacket());
        isNightmareOverlay = WellRestedClientState.isNightmareMode();
        isOverlayVisible = true;
        overlayAlpha = 1f;
        outgoingSlide = null;
        outgoingAlpha = 0f;
        incomingAlpha = 1f;
        currentSlide = getNightmareAwareSlide();
        nextSlide = getNightmareAwareSlide();
        currentSlideDuration = config.getRandomSlideDisplayTime();
        visibleStartTime = System.currentTimeMillis();
        slideState = SlideState.VISIBLE;
        if (isNightmareOverlay) {
            nightmareField.reset(System.currentTimeMillis());
        } else {
            starField.reset(System.currentTimeMillis());
        }
    }

    private void onSleepEnd() {
        currentSlide = null;
        nextSlide = null;
        outgoingSlide = null;
        slideState = SlideState.HIDDEN;
        incomingAlpha = 0f;
        outgoingAlpha = 0f;
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
        if (isOverlayVisible && overlayAlpha < 1f) overlayAlpha = Math.min(1f, overlayAlpha + 0.02f);
    }

    private void updateSlideState() {
        long now = System.currentTimeMillis();
        switch (slideState) {
            case VISIBLE -> {
                incomingAlpha = 1f;
                outgoingAlpha = 0f;
                if (now - visibleStartTime >= currentSlideDuration) {
                    outgoingSlide = currentSlide;
                    currentSlide = nextSlide != null ? nextSlide : getNightmareAwareSlide();
                    nextSlide = getNightmareAwareSlide();
                    transitionStart = now;
                    outgoingAlpha = 1f;
                    incomingAlpha = 0f;
                    slideState = SlideState.TRANSITION;
                }
            }
            case TRANSITION -> {
                long elapsed = now - transitionStart;
                float outP = clamp01(elapsed / (float) Math.max(1, config.getFadeOutDurationMs()));
                float inP = clamp01(elapsed / (float) Math.max(1, config.getFadeInDurationMs()));
                outgoingAlpha = 1f - easeInOut(outP);
                incomingAlpha = easeInOut(inP);
                if (outP >= 1f && inP >= 1f) {
                    outgoingSlide = null;
                    outgoingAlpha = 0f;
                    incomingAlpha = 1f;
                    visibleStartTime = now;
                    currentSlideDuration = config.getRandomSlideDisplayTime();
                    slideState = SlideState.VISIBLE;
                }
            }
            case HIDDEN -> {
                incomingAlpha = 0f;
                outgoingAlpha = 0f;
            }
        }
    }

    private float clamp01(float v) {
        return v < 0f ? 0f : Math.min(v, 1f);
    }

    private float easeInOut(float t) {
        t = clamp01(t);
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    private int clampAlpha(float a) {
        int v = Math.round(a * 255f);
        return v < 0 ? 0 : Math.min(v, 255);
    }

    public void renderOverlayOnly(GuiGraphics context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || !config.isEnableOverlay()) return;
        if (overlayAlpha <= 0 && !isOverlayVisible) return;
        renderOverlay(context, screenWidth, screenHeight);
    }

    public void renderContentOnly(GuiGraphics context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || !config.isEnableOverlay()) return;
        if (overlayAlpha <= 0 && !isOverlayVisible) return;
        renderContent(context, screenWidth, screenHeight);
    }

    private void renderOverlay(GuiGraphics context, int w, int h) {
        int rgb = isNightmareOverlay ? 0x1A0000 : 0x000000;
        float strength = config.getOverlayOpacity() * overlayAlpha;

        int baseA = clampAlpha(strength * 0.6f);
        context.fill(0, 0, w, h, (baseA << 24) | rgb);

        int edgeA = clampAlpha(strength);
        int bandH = Math.round(h * 0.42f);
        int bandW = Math.round(w * 0.30f);
        context.fillGradient(0, 0, w, bandH, (edgeA << 24) | rgb, rgb);
        context.fillGradient(0, h - bandH, w, h, rgb, (edgeA << 24) | rgb);
        fillHorizontalGradient(context, 0, bandW, h, edgeA, 0, rgb);
        fillHorizontalGradient(context, w - bandW, w, h, 0, edgeA, rgb);
    }

    static void fillHorizontalGradient(GuiGraphics context, int x1, int x2, int y2, int alphaLeft, int alphaRight, int rgb) {
        int step = 3;
        int width = x2 - x1;
        if (width <= 0) return;
        for (int x = x1; x < x2; x += step) {
            float t = (float) (x - x1) / width;
            int a = Math.round(alphaLeft + (alphaRight - alphaLeft) * t);
            int xe = Math.min(x + step, x2);
            context.fill(x, 0, xe, y2, (a << 24) | rgb);
        }
    }

    private void renderContent(GuiGraphics context, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        float baseScale = Math.min(w / BASE_W, h / BASE_H);
        float imageScale = baseScale * config.getOverlayImageScale();
        float textScale = baseScale * config.getOverlayTextScale();
        int imageSize = Math.max(1, Math.round(VIRTUAL_IMAGE_SIZE * imageScale));
        int gap = Math.round(VIRTUAL_GAP * imageScale);
        int textAreaWidth = Math.round(VIRTUAL_TEXT_WIDTH * textScale);
        int lineHeight = font.lineHeight + VIRTUAL_LINE_SPACING;
        int centerX = w / 2;
        boolean showImage = config.isEnableImage();

        float imageBlockShift = showImage
                ? (VIRTUAL_IMAGE_SIZE + VIRTUAL_GAP) * (1.0f - config.getOverlayImageScale())
                : 0.0f;
        float textBlockShift = VIRTUAL_TEXT_BLOCK * (1.0f - config.getOverlayTextScale());
        int groupShift = Math.round((imageBlockShift + textBlockShift) * baseScale / 2.0f);
        int imageTop = Math.round(h * GROUP_TOP_PERCENT) + groupShift;

        if (config.isEnableStarDust()) {
            if (isNightmareOverlay) {
                nightmareField.render(context, w, h, overlayAlpha);
            } else {
                starField.render(context, w, h, overlayAlpha);
            }
        }

        if (showImage) {
            renderImage(context, centerX - imageSize / 2, imageTop, imageSize);
        }
        int textTop = imageTop + (showImage ? imageSize + gap : 0);

        if (outgoingSlide != null && outgoingAlpha > 0.01f) {
            TextLayout outgoing = outgoingCache.get(outgoingSlide, font, textAreaWidth, textScale);
            drawLayout(context, font, outgoing, centerX, textTop, lineHeight, outgoingAlpha);
        }

        TextLayout currentLayout = null;
        if (currentSlide != null && incomingAlpha > 0.01f) {
            currentLayout = currentCache.get(currentSlide, font, textAreaWidth, textScale);
            drawLayout(context, font, currentLayout, centerX, textTop, lineHeight, incomingAlpha);
        }

        if (config.isShowSlideProgress() && slideState == SlideState.VISIBLE && currentLayout != null) {
            int textBottom = textTop + Math.round(currentLayout.lines().size() * lineHeight * currentLayout.scale());
            drawProgress(context, centerX, textBottom + Math.round(18 * baseScale), baseScale, slideProgress());
        }
    }

    private void renderImage(GuiGraphics context, int x, int y, int size) {
        ResourceLocation texture = isNightmareOverlay
                ? SKULL_TEXTURE
                : MOON_PHASE_TEXTURES[Math.floorMod(moonPhase, MOON_PHASE_TEXTURES.length)];

        mt.client.ui.summary.RenderHelper.blitTexture(context, texture, x, y, size, size,
                overlayAlpha * config.getImageOpacity());
    }

    private static TextLayout layoutText(String text, Font font, int textAreaWidth, float uiScale) {
        float base = VIRTUAL_TEXT_SCALE * uiScale;
        float minScale = base * 0.60f;
        float maxScale = base * 1.25f;
        float scale = base;
        List<String> lines = wrapText(text, font, Math.max(1, Math.round(textAreaWidth / scale)));

        int guard = 0;
        while (lines.size() > MAX_TEXT_LINES && scale > minScale && guard++ < 20) {
            scale = Math.max(minScale, scale - base * 0.08f);
            lines = wrapText(text, font, Math.max(1, Math.round(textAreaWidth / scale)));
        }
        guard = 0;
        while (lines.size() <= SHORT_TEXT_LINES && scale < maxScale && guard++ < 20) {
            float trial = Math.min(maxScale, scale + base * 0.08f);
            List<String> trialLines = wrapText(text, font, Math.max(1, Math.round(textAreaWidth / trial)));
            if (trialLines.size() > SHORT_TEXT_LINES) break;
            scale = trial;
            lines = trialLines;
        }
        return new TextLayout(lines, scale);
    }

    private void drawLayout(GuiGraphics context, Font font, TextLayout layout, int centerX, int top, int lineHeight, float alpha01) {
        int a = clampAlpha(config.getTextOpacity() * alpha01);
        if (a <= 0) return;
        int textColor = (a << 24) | 0xFFFFFF;

        context.pose().pushPose();
        context.pose().translate(centerX, top, 0);
        context.pose().scale(layout.scale(), layout.scale(), 1.0f);
        List<String> lines = layout.lines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = font.width(line);
            context.drawString(font, line, -lineWidth / 2, i * lineHeight, textColor, true);
        }
        context.pose().popPose();
    }

    private void drawProgress(GuiGraphics context, int centerX, int y, float uiScale, float frac) {
        int barWidth = Math.max(20, Math.round(VIRTUAL_PROGRESS_WIDTH * uiScale));
        int barHeight = Math.max(1, Math.round(2 * uiScale));
        int x = centerX - barWidth / 2;

        int trackA = clampAlpha(overlayAlpha * 0.18f);
        int fillA = clampAlpha(overlayAlpha * 0.65f);
        context.fill(x, y, x + barWidth, y + barHeight, (trackA << 24) | 0xFFFFFF);
        int fw = Math.round(barWidth * (1f - frac));
        if (fw > 0) context.fill(x, y, x + fw, y + barHeight, (fillA << 24) | 0xFFFFFF);
    }

    private float slideProgress() {
        if (currentSlideDuration <= 0) return 0f;
        long elapsed = System.currentTimeMillis() - visibleStartTime;
        return clamp01(elapsed / (float) currentSlideDuration);
    }

    static List<String> wrapText(String text, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            if (word.isEmpty()) continue;

            if (font.width(word) > maxWidth) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }
                breakByCharacters(word, font, maxWidth, lines, currentLine);
                continue;
            }

            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(' ');
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    private static void breakByCharacters(String word, Font font, int maxWidth, List<String> lines, StringBuilder tail) {
        StringBuilder chunk = new StringBuilder();
        int i = 0;
        while (i < word.length()) {
            int codePoint = word.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String character = word.substring(i, i + charCount);
            i += charCount;

            if (!chunk.isEmpty() && font.width(chunk + character) > maxWidth) {
                lines.add(chunk.toString());
                chunk.setLength(0);
            }
            chunk.append(character);
        }
        tail.append(chunk);
    }

    public boolean shouldHideCrosshair() {
        return sleepStateManager.isSleeping() && isOverlayVisible;
    }

    public boolean shouldHideChat() {
        return config.isHideChatWhenSleeping() && sleepStateManager.isSleeping() && isOverlayVisible;
    }

    public boolean shouldHideHudMessages() {
        return config.isHideHudMessagesWhenSleeping() && sleepStateManager.isSleeping() && isOverlayVisible;
    }
}
