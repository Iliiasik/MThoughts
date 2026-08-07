package mt.client.ui;

import mt.client.config.ClientConfig;
import mt.client.ui.summary.SummaryConstants;
import mt.client.ui.summary.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class SleepingPlayersHud {
    private static final int TEXTURE_WIDTH = 260;
    private static final int TEXTURE_HEIGHT = 160;

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
        float delta = Math.min(MAX_FRAME_DELTA_MS, Math.max(0.0f, now - previous));
        return delta / FADE_DURATION_MS;
    }

    public static void render(GuiGraphics context, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
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

        float scale = Math.max(0.8f, Math.min(1.5f, Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f)));

        int hudHeight = (int)(55 * scale);
        int hudWidth = (int)(hudHeight * (TEXTURE_WIDTH / (float)TEXTURE_HEIGHT));
        int hudX = (int)(10 * scale);
        int hudY = (int)(10 * scale);

        int color = ARGB.colorFromFloat(displayAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(hudX, hudY);
        context.pose().scale(hudWidth / (float)TEXTURE_WIDTH, hudHeight / (float)TEXTURE_HEIGHT);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getSleepingHudTexture(),
            0, 0, 0.0f, 0.0f, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, color);
        context.pose().popMatrix();

        renderText(context, textRenderer, hudX, hudY, hudWidth, scale);
    }

    private static void renderText(GuiGraphics context, Font textRenderer, int hudX, int hudY, int hudWidth, float scale) {
        String sleepText = sleepingCount + " / " + totalPlayers;
        Component titleText = Component.translatable("midnightthoughts.hud.sleeping");

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int textAlpha = (int)(displayAlpha * 255);
        int titleColor = (textAlpha << 24) | colors.sleepingHudTitleColor();

        float textScale = scale * 1.1f;
        int titleWidth = (int)(textRenderer.width(titleText) * textScale);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        int titleY = hudY + (int)(14 * scale);

        context.pose().pushMatrix();
        context.pose().translate(titleX, titleY);
        context.pose().scale(textScale, textScale);
        context.drawString(textRenderer, titleText, 0, 0, titleColor, false);
        context.pose().popMatrix();

        int sleepColor = (textAlpha << 24) | colors.sleepingHudCountColor();
        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = (int)(textRenderer.width(sleepText) * sleepTextScale);
        int sleepX = hudX + (hudWidth - sleepTextWidth) / 2;
        int sleepY = hudY + (int)(32 * scale);

        context.pose().pushMatrix();
        context.pose().translate(sleepX, sleepY);
        context.pose().scale(sleepTextScale, sleepTextScale);
        context.drawString(textRenderer, sleepText, 0, 0, sleepColor, false);
        context.pose().popMatrix();
    }
}
