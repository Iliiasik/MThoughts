package mt.client.ui;

import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import mt.client.ui.summary.SummaryConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

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
        float delta = Math.clamp((float) (now - previous), 0.0f, MAX_FRAME_DELTA_MS);
        return delta / FADE_DURATION_MS;
    }

    public static void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

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

        if (displayAlpha <= 0.01f) return;

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        Font font = client.font;

        float scale = Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f);
        scale = Math.clamp(scale, 0.8f, 1.5f);

        int hudHeight = (int) (55 * scale);
        int hudWidth = (int) (hudHeight * (TEXTURE_WIDTH / (float) TEXTURE_HEIGHT));
        int hudX = (int) (10 * scale);
        int hudY = (int) (10 * scale);

        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getSleepingHudTexture(), hudX, hudY,
                0.0f, 0.0f, hudWidth, hudHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT,
                0xFFFFFFFF);

        String sleepText = sleepingCount + " / " + totalPlayers;
        Component titleText = Component.translatable("midnightthoughts.hud.sleeping");

        int textAlpha = (int) (displayAlpha * 255);
        int titleColor = (textAlpha << 24) | colors.sleepingHudTitleColor();

        float textScale = scale * 1.1f;
        int titleWidth = (int) (font.width(titleText) * textScale);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        int titleY = hudY + (int) (14 * scale);

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(titleX, titleY);
        matrices.scale(textScale, textScale);
        context.text(font, titleText, 0, 0, titleColor, false);
        matrices.popMatrix();

        int sleepColor = (textAlpha << 24) | colors.sleepingHudCountColor();
        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = (int) (font.width(sleepText) * sleepTextScale);
        int sleepX = hudX + (hudWidth - sleepTextWidth) / 2;
        int sleepY = hudY + (int) (32 * scale);

        matrices.pushMatrix();
        matrices.translate(sleepX, sleepY);
        matrices.scale(sleepTextScale, sleepTextScale);
        context.text(font, sleepText, 0, 0, sleepColor, false);
        matrices.popMatrix();
    }
}
