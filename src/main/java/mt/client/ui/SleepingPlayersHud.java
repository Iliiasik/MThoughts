package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.config.MidnightThoughtsConfig;
import mt.client.ui.summary.SummaryConstants;
import mt.client.ui.summary.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class SleepingPlayersHud {
    private static final int TEXTURE_WIDTH = 260;
    private static final int TEXTURE_HEIGHT = 160;

    private static int sleepingCount = 0;
    private static int totalPlayers = 0;
    private static float displayAlpha = 0.0f;
    private static final float FADE_SPEED = 0.1f;

    public static void updateSleepingCount(int sleeping, int total) {
        sleepingCount = sleeping;
        totalPlayers = total;
    }

    public static void render(GuiGraphics context, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        boolean shouldShow = sleepingCount > 0 && totalPlayers > 0;

        if (shouldShow) {
            displayAlpha = Math.min(1.0f, displayAlpha + FADE_SPEED);
        } else {
            displayAlpha = Math.max(0.0f, displayAlpha - FADE_SPEED);
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

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SummaryConstants.getSleepingHudTexture());
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

        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);

        int textAlpha = (int)(displayAlpha * 255);
        int titleColor = (textAlpha << 24) | colors.sleepingHudTitleColor();

        float textScale = scale * 1.1f;
        int titleWidth = (int)(textRenderer.width(titleText) * textScale);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        int titleY = hudY + (int)(14 * scale);

        context.pose().pushPose();
        context.pose().translate(titleX, titleY, 0);
        context.pose().scale(textScale, textScale, 1.0f);
        context.drawString(textRenderer, titleText, 0, 0, titleColor, false);
        context.pose().popPose();

        int sleepColor = (textAlpha << 24) | colors.sleepingHudCountColor();
        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = (int)(textRenderer.width(sleepText) * sleepTextScale);
        int sleepX = hudX + (hudWidth - sleepTextWidth) / 2;
        int sleepY = hudY + (int)(32 * scale);

        context.pose().pushPose();
        context.pose().translate(sleepX, sleepY, 0);
        context.pose().scale(sleepTextScale, sleepTextScale, 1.0f);
        context.drawString(textRenderer, sleepText, 0, 0, sleepColor, true);
        context.pose().popPose();
    }
}
