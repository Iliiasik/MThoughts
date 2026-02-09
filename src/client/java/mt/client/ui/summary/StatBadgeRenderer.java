package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class StatBadgeRenderer {

    public static int render(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                             int startX, int badgeY1, int badgeY2, BadgeDimensions dims,
                             float animProgress, float fadeAlpha) {

        int animBlocks = (int)(player.blocksDestroyed() * animProgress);
        int animDistance = (int)(player.distanceWalked() * animProgress / 100);
        int animMobs = (int)(player.mobsKilled() * animProgress);
        int animDeaths = (int)(player.deaths() * animProgress);
        int animJumps = (int)(player.jumps() * animProgress);

        String blocksText = Text.translatable("midnightthoughts.summary.blocks").getString() + " " + animBlocks;
        String distanceText = Text.translatable("midnightthoughts.summary.distance").getString() + " " + animDistance;
        String mobsText = Text.translatable("midnightthoughts.summary.mobs").getString() + " " + animMobs;
        String deathsText = Text.translatable("midnightthoughts.summary.deaths").getString() + " " + animDeaths;
        String jumpsText = Text.translatable("midnightthoughts.summary.jumps").getString() + " " + animJumps;

        int currentX = startX;
        currentX = renderBadge(context, textRenderer, currentX, badgeY1, dims.height(), dims.padding(),
                              blocksText, 0x3d5a80, dims.textScale(), fadeAlpha);
        currentX += dims.spacing();
        currentX = renderBadge(context, textRenderer, currentX, badgeY1, dims.height(), dims.padding(),
                              distanceText, 0x2a6041, dims.textScale(), fadeAlpha);
        currentX += dims.spacing();
        int statsEndX = renderBadge(context, textRenderer, currentX, badgeY1, dims.height(), dims.padding(),
                                   mobsText, 0x7a3d3d, dims.textScale(), fadeAlpha);

        currentX = startX;
        currentX = renderBadge(context, textRenderer, currentX, badgeY2, dims.height(), dims.padding(),
                              deathsText, 0x4a3d5a, dims.textScale(), fadeAlpha);
        currentX += dims.spacing();
        renderBadge(context, textRenderer, currentX, badgeY2, dims.height(), dims.padding(),
                   jumpsText, 0x5a4a3d, dims.textScale(), fadeAlpha);

        return statsEndX;
    }

    private static int renderBadge(DrawContext context, TextRenderer textRenderer, int x, int y, int height,
                                   int padding, String text, int bgColor, float textScale, float fadeAlpha) {
        int textWidth = (int)(textRenderer.getWidth(text) * textScale);
        int badgeWidth = textWidth + padding * 2;

        int alpha = (int)(fadeAlpha * 180);
        int bg = (alpha << 24) | bgColor;
        context.fill(x, y, x + badgeWidth, y + height, bg);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xeeeeee;
        int textY = y + (height - (int)(8 * textScale)) / 2;

        RenderUtils.renderScaledText(context, textRenderer, text, x + padding, textY, textColor, textScale, false);

        return x + badgeWidth;
    }
}

