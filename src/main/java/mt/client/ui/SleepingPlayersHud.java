package mt.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SleepingPlayersHud {
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

        String sleepText = sleepingCount + " / " + totalPlayers;
        Component titleText = Component.translatable("midnightthoughts.hud.sleeping");

        int innerPadding = 6;
        int titleWidth = textRenderer.width(titleText);
        int sleepTextWidth = textRenderer.width(sleepText);
        int maxTextWidth = Math.max(titleWidth, sleepTextWidth);

        int boxWidth = maxTextWidth + innerPadding * 2 + 4;
        int boxHeight = 32;
        int boxX = screenWidth - boxWidth - 10;
        int boxY = 10;

        int alpha = (int)(displayAlpha * 220);
        int bgColor = (alpha << 24) | 0x1a1a2e;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);

        int borderAlpha = (int)(displayAlpha * 255);
        int borderColor = (borderAlpha << 24) | 0x4a4a6a;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 2, borderColor);
        context.fill(boxX, boxY + boxHeight - 2, boxX + boxWidth, boxY + boxHeight, borderColor);
        context.fill(boxX, boxY, boxX + 2, boxY + boxHeight, borderColor);
        context.fill(boxX + boxWidth - 2, boxY, boxX + boxWidth, boxY + boxHeight, borderColor);

        int cornerColor = (borderAlpha << 24) | 0x7a7aaa;
        context.fill(boxX, boxY, boxX + 4, boxY + 4, cornerColor);
        context.fill(boxX + boxWidth - 4, boxY, boxX + boxWidth, boxY + 4, cornerColor);
        context.fill(boxX, boxY + boxHeight - 4, boxX + 4, boxY + boxHeight, cornerColor);
        context.fill(boxX + boxWidth - 4, boxY + boxHeight - 4, boxX + boxWidth, boxY + boxHeight, cornerColor);

        int textAlpha = (int)(displayAlpha * 255);
        int titleColor = (textAlpha << 24) | 0xaaaacc;
        int titleX = boxX + (boxWidth - titleWidth) / 2;
        textRenderer.drawInBatch(titleText.getString(), titleX, boxY + 5, titleColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

        int sleepColor = (textAlpha << 24) | 0xffee88;
        int sleepX = boxX + (boxWidth - sleepTextWidth) / 2;
        textRenderer.drawInBatch(sleepText, sleepX, boxY + 18, sleepColor, true, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    public static void reset() {
        sleepingCount = 0;
        totalPlayers = 0;
        displayAlpha = 0.0f;
    }

    public static boolean isShowingSleepingHud() {
        return sleepingCount > 0 && totalPlayers > 0;
    }
}
