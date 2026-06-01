package mt.client.ui;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import mt.client.config.ThemeColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

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

    public static void render(DrawContext context, int screenWidth, int screenHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
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

        String theme = ClientConfig.getInstance().getEffectiveTheme();
        Identifier sleepingHudTexture = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/sleeping_hud.png");
        ThemeColors.ThemeColor colors = ThemeColors.getThemeColors(theme);
        TextRenderer textRenderer = client.textRenderer;

        float scale = Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f);
        scale = Math.max(0.8f, Math.min(1.5f, scale));

        int hudHeight = (int)(55 * scale);
        int hudWidth = (int)(hudHeight * (TEXTURE_WIDTH / (float)TEXTURE_HEIGHT));
        int hudX = (int)(10 * scale);
        int hudY = (int)(10 * scale);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, sleepingHudTexture, hudX, hudY,
            0.0f, 0.0f, hudWidth, hudHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT,
            TEXTURE_WIDTH, TEXTURE_HEIGHT,
            0xFFFFFFFF);

        String sleepText = sleepingCount + " / " + totalPlayers;
        Text titleText = Text.translatable("midnightthoughts.hud.sleeping");

        int textAlpha = (int)(displayAlpha * 255);
        int titleColor = (textAlpha << 24) | colors.sleepingHudTitleColor();

        float textScale = scale * 1.1f;
        int titleWidth = (int)(textRenderer.getWidth(titleText) * textScale);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        int titleY = hudY + (int)(14 * scale);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(titleX, titleY);
        matrices.scale(textScale, textScale);
        context.drawText(textRenderer, titleText, 0, 0, titleColor, false);
        matrices.popMatrix();

        int sleepColor = (textAlpha << 24) | colors.sleepingHudCountColor();
        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = (int)(textRenderer.getWidth(sleepText) * sleepTextScale);
        int sleepX = hudX + (hudWidth - sleepTextWidth) / 2;
        int sleepY = hudY + (int)(32 * scale);

        matrices.pushMatrix();
        matrices.translate(sleepX, sleepY);
        matrices.scale(sleepTextScale, sleepTextScale);
        context.drawText(textRenderer, sleepText, 0, 0, sleepColor, false);
        matrices.popMatrix();
    }
}
