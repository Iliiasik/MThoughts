package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SleepingPlayersHud {
    private static final Identifier SLEEPING_HUD_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/sleeping_hud.png");
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

        TextRenderer textRenderer = client.textRenderer;

        float scale = Math.min(screenWidth / 1920.0f, screenHeight / 1080.0f);
        scale = Math.max(0.8f, Math.min(1.5f, scale));

        int hudHeight = (int)(55 * scale);
        int hudWidth = (int)(hudHeight * (TEXTURE_WIDTH / (float)TEXTURE_HEIGHT));
        int hudX = (int)(10 * scale);
        int hudY = (int)(10 * scale);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SLEEPING_HUD_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, displayAlpha);
        RenderSystem.enableBlend();

        context.drawTexture(SLEEPING_HUD_TEXTURE, hudX, hudY, hudWidth, hudHeight,
            0.0f, 0.0f, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        String sleepText = sleepingCount + " / " + totalPlayers;
        Text titleText = Text.translatable("midnightthoughts.hud.sleeping");

        int textAlpha = (int)(displayAlpha * 255);
        int titleColor = (textAlpha << 24) | 0xd8b3e6;

        float textScale = scale * 1.1f;
        int titleWidth = (int)(textRenderer.getWidth(titleText) * textScale);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        int titleY = hudY + (int)(14 * scale);

        context.getMatrices().push();
        context.getMatrices().translate(titleX, titleY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, titleText, 0, 0, titleColor, false);
        context.getMatrices().pop();

        int sleepColor = (textAlpha << 24) | 0xffee88;
        float sleepTextScale = scale * 1.2f;
        int sleepTextWidth = (int)(textRenderer.getWidth(sleepText) * sleepTextScale);
        int sleepX = hudX + (hudWidth - sleepTextWidth) / 2;
        int sleepY = hudY + (int)(32 * scale);

        context.getMatrices().push();
        context.getMatrices().translate(sleepX, sleepY, 0);
        context.getMatrices().scale(sleepTextScale, sleepTextScale, 1.0f);
        context.drawText(textRenderer, sleepText, 0, 0, sleepColor, true);
        context.getMatrices().pop();
    }

    public static void reset() {
        sleepingCount = 0;
        totalPlayers = 0;
        displayAlpha = 0.0f;
    }

}

