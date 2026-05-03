package mt.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.manager.WellRestedClientState;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.service.SlideService;
import mt.network.NetworkHandler;
import mt.network.packet.RequestMoonPhasePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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

    private static final int IMAGE_ORIGINAL_WIDTH = 360;
    private static final int IMAGE_ORIGINAL_HEIGHT = 360;
    private static final float IMAGE_SCALE_PERCENT = 0.35f;
    private static final float GAP_PERCENT = 0.03f;
    private static final float TEXT_AREA_WIDTH_PERCENT = 0.35f;
    private static final float MIN_TEXT_SCALE = 1.5f;
    private static final float MAX_TEXT_SCALE = 3.0f;
    private static final long SLEEP_DEBOUNCE_MS = 200;
    private static final long SLIDE_DELAY_MS = 500;

    private final SleepStateManager sleepStateManager;
    private final SlideService slideService;
    private final MidnightThoughtsConfig config;

    private Slide currentSlide;
    private Slide nextSlide;
    private long stateStartTime;
    private long lastSleepEndTime;
    private int currentSlideDuration;
    private boolean isOverlayVisible = false;
    private int moonPhase = 0;
    private boolean isNightmareOverlay = false;

    private enum SlideState {
        HIDDEN, VISIBLE, DELAY
    }

    private SlideState slideState = SlideState.HIDDEN;

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
            updateSlideState();
        }
    }

    private void onSleepStart() {
        NetworkHandler.CHANNEL.sendToServer(new RequestMoonPhasePacket());
        isNightmareOverlay = WellRestedClientState.isNightmareMode();
        isOverlayVisible = true;
        currentSlide = getNightmareAwareSlide();
        nextSlide = getNightmareAwareSlide();
        currentSlideDuration = config.getRandomSlideDisplayTime();
        stateStartTime = System.currentTimeMillis();
        slideState = SlideState.VISIBLE;
    }

    private void onSleepEnd() {
        currentSlide = null;
        nextSlide = null;
        slideState = SlideState.HIDDEN;
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

    private void updateSlideState() {
        long now = System.currentTimeMillis();
        long elapsedInState = now - stateStartTime;

        if (slideState == SlideState.VISIBLE) {
            if (elapsedInState >= currentSlideDuration) {
                slideState = SlideState.DELAY;
                stateStartTime = now;
            }
        } else if (slideState == SlideState.DELAY) {
            if (elapsedInState >= SLIDE_DELAY_MS) {
                currentSlide = nextSlide != null ? nextSlide : getNightmareAwareSlide();
                nextSlide = null;
                currentSlideDuration = config.getRandomSlideDisplayTime();
                slideState = SlideState.VISIBLE;
                stateStartTime = now;
            }
        }
    }

    public void renderOverlayOnly(GuiGraphics context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || config.isEnableOverlay() || !isOverlayVisible) return;
        renderOverlay(context, screenWidth, screenHeight);
    }

    public void renderContentOnly(GuiGraphics context, int screenWidth, int screenHeight) {
        if (!sleepStateManager.isSleeping() || config.isEnableOverlay() || !isOverlayVisible) return;
        renderContent(context, screenWidth, screenHeight);
    }

    private void renderOverlay(GuiGraphics context, int screenWidth, int screenHeight) {
        int alpha = (int) (config.getOverlayOpacity() * 255);
        int overlayColor = isNightmareOverlay
                ? (alpha << 24) | 0x1A0000
                : (alpha << 24);
        context.fill(0, 0, screenWidth, screenHeight, overlayColor);
    }

    private void renderContent(GuiGraphics context, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

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

        if (currentSlide != null && slideState == SlideState.VISIBLE) {
            int scaledTextWidth = (int) (textAreaWidth / textScale);
            List<String> lines = wrapText(currentSlide.text(), font, scaledTextWidth);
            renderSlideText(context, font, lines, textAreaX, centerY, textScale);
        }
    }

    private float calculateTextScale(int screenHeight) {
        float baseScale = screenHeight / 400f;
        return Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, baseScale));
    }

    private void renderImage(GuiGraphics context, int x, int y, int width, int height) {
        ResourceLocation texture = isNightmareOverlay
                ? SKULL_TEXTURE
                : MOON_PHASE_TEXTURES[moonPhase % MOON_PHASE_TEXTURES.length];

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.pose().pushPose();
        context.pose().translate(x, y, 0);
        context.pose().scale((float) width / IMAGE_ORIGINAL_WIDTH, (float) height / IMAGE_ORIGINAL_HEIGHT, 1.0f);
        context.blit(texture, 0, 0, 0.0f, 0.0f, IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT, IMAGE_ORIGINAL_WIDTH, IMAGE_ORIGINAL_HEIGHT);
        context.pose().popPose();
        RenderSystem.disableBlend();
    }

    private void renderSlideText(GuiGraphics context, Font font, List<String> lines, int areaX, int centerY, float scale) {
        int textColor = 0xFFFFFFFF;
        int lineHeight = font.lineHeight + 4;
        int totalTextHeight = (int) (lines.size() * lineHeight * scale);
        int textY = centerY - totalTextHeight / 2;

        context.pose().pushPose();
        context.pose().translate(areaX, textY, 0);
        context.pose().scale(scale, scale, 1.0f);

        for (int i = 0; i < lines.size(); i++) {
            context.drawString(font, lines.get(i), 0, i * lineHeight, textColor, true);
        }

        context.pose().popPose();
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