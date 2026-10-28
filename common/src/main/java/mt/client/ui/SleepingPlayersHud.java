package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.ui.engine.RenderUtils;
import mt.client.ui.summary.SummaryConstants;
import mt.client.ui.summary.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SleepingPlayersHud {
    private static final int TEXTURE_WIDTH = SummaryConstants.SLEEPING_HUD_TEXTURE_WIDTH;
    private static final int TEXTURE_HEIGHT = SummaryConstants.SLEEPING_HUD_TEXTURE_HEIGHT;

    private static int sleepingCount = 0;
    private static int totalPlayers = 0;
    private static float displayAlpha = 0.0f;
    private static long lastRenderTime = 0L;
    private static final float FADE_DURATION_MS = 160.0f;
    private static final float MAX_FRAME_DELTA_MS = 100.0f;

    public static void updateSleepingCount(int sleeping, int total) {
        sleepingCount = sleeping;
        totalPlayers = total;
    }

    public static void reset() {
        sleepingCount = 0;
        totalPlayers = 0;
        displayAlpha = 0.0f;
        lastRenderTime = 0L;
    }

    static float fadeStep(long now, long previous) {
        if (previous <= 0L) return 1.0f / FADE_DURATION_MS * 16.0f;
        float delta = Math.clamp((float) (now - previous), 0.0f, MAX_FRAME_DELTA_MS);
        return delta / FADE_DURATION_MS;
    }

    public static void render(GuiGraphics context, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        if (mt.config.MidnightThoughtsConfig.getInstance().isHideSleepingPlayersHud()) {
            return;
        }

        boolean shouldShow = sleepingCount > 0 && totalPlayers > 0;

        long now = System.currentTimeMillis();
        float step = fadeStep(now, lastRenderTime);
        lastRenderTime = now;

        if (shouldShow) {
            displayAlpha = Math.min(1.0f, displayAlpha + step);
        } else {
            displayAlpha = Math.max(0.0f, displayAlpha - step);
        }

        if (displayAlpha <= 0.01f) {
            return;
        }

        Font textRenderer = client.font;

        float scale = Math.clamp(Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f), 0.8f, 1.5f);

        int hudScale = Math.max(1, Math.round(55 * scale / (float) TEXTURE_HEIGHT));
        int hudHeight = TEXTURE_HEIGHT * hudScale;
        int hudWidth = TEXTURE_WIDTH * hudScale;
        int hudX = Math.round(10 * scale);
        int hudY = Math.round(10 * scale);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, displayAlpha);
        RenderSystem.enableBlend();

        context.blit(SummaryConstants.getSleepingHudTexture(), hudX, hudY, hudWidth, hudHeight,
            0.0f, 0.0f, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        renderText(context, textRenderer, hudX, hudY, hudWidth, hudHeight, scale);
    }

    private static void renderText(GuiGraphics context, Font textRenderer, int hudX, int hudY, int hudWidth, int hudHeight, float scale) {
        String sleepText = sleepingCount + " / " + totalPlayers;
        Component titleText = Component.translatable("midnightthoughts.hud.sleeping");

        ThemeColors.TextStyle style = ThemeColors.current().sleepingHud();
        int textColor = style.argb(displayAlpha);

        float textScale = scale * 1.1f;
        int titleWidth = RenderUtils.scaledWidth(textRenderer, titleText, textScale);
        int titleX = RenderUtils.centeredX(hudX, hudWidth, titleWidth);
        int titleY = hudY + Math.round(hudHeight * (14.0f / 55.0f));

        context.pose().pushPose();
        context.pose().translate(titleX, titleY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, titleText, 0, 0, textColor, style.shadow());
        context.pose().popPose();

        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = RenderUtils.scaledWidth(textRenderer, sleepText, sleepTextScale);
        int sleepX = RenderUtils.centeredX(hudX, hudWidth, sleepTextWidth);
        int sleepY = hudY + Math.round(hudHeight * (32.0f / 55.0f));

        context.pose().pushPose();
        context.pose().translate(sleepX, sleepY, 0);
        context.pose().scale(sleepTextScale, sleepTextScale, 1.0f);
        context.drawString(textRenderer, sleepText, 0, 0, textColor, style.shadow());
        context.pose().popPose();
    }
}
