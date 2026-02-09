package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.gl.RenderPipelines;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class PlayerRowRenderer {

    public static void render(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                              int x, int y, SummaryDimensions dims, float fadeAlpha, long animationStartTime,
                              List<AchievementTooltipArea> achievementAreas) {

        float contentPaddingSides = SummaryConstants.FRAME_CONTENT_PADDING_SIDES * (dims.panelWidth / 1000.0f);
        int rowWidth = dims.panelWidth - (int)(contentPaddingSides * 2);
        int rowHeight = dims.playerRowHeight - (int)(8 * dims.uiScale);

        renderBackground(context, player, x, y, rowWidth, rowHeight, fadeAlpha);

        if (player.isMvp()) {
            renderMvpBadge(context, x, y, rowWidth, dims, fadeAlpha);
        }

        renderHead(context, player, x, y, rowHeight, dims);
        renderName(context, textRenderer, player, x, y, dims, fadeAlpha);
        renderStats(context, textRenderer, player, x, y, rowWidth, dims, fadeAlpha, animationStartTime, achievementAreas);
    }

    private static void renderBackground(DrawContext context, DailySummaryPacket.PlayerDailySummary player,
                                         int x, int y, int rowWidth, int rowHeight, float fadeAlpha) {
        int rowBgAlpha = (int)(fadeAlpha * 90);
        int rowBg;
        if (player.isMvp()) {
            rowBg = (rowBgAlpha << 24) | 0x4a4020;
        } else {
            rowBg = ((int)(fadeAlpha * 80) << 24) | 0x2a2a4a;
        }
        context.fill(x, y, x + rowWidth, y + rowHeight, rowBg);
    }

    private static void renderMvpBadge(DrawContext context, int x, int y, int rowWidth, SummaryDimensions dims, float fadeAlpha) {
        int crownHeight = (int)((dims.isCompactMode ? 14 : 18) * dims.uiScale);
        int crownWidth = (int)(crownHeight * 1.75f);
        int mvpBadgeX = x + rowWidth - crownWidth;
        int mvpBadgeY = y - crownHeight / 2;

        int badgeAlpha = (int)(fadeAlpha * 240);
        int badgeBg = (badgeAlpha << 24) | 0x8b7320;
        int badgeBorder = (badgeAlpha << 24) | SummaryConstants.MVP_GOLD_COLOR;

        context.fill(mvpBadgeX - 1, mvpBadgeY - 1, mvpBadgeX + crownWidth + 1, mvpBadgeY + crownHeight + 1, badgeBorder);
        context.fill(mvpBadgeX, mvpBadgeY, mvpBadgeX + crownWidth, mvpBadgeY + crownHeight, badgeBg);

        renderCrown(context, mvpBadgeX, mvpBadgeY, crownWidth, crownHeight);
    }

    private static void renderCrown(DrawContext context, int badgeX, int badgeY, int badgeWidth, int badgeHeight) {
        int iconPadding = 2;
        int iconHeight = badgeHeight - iconPadding * 2;
        int iconWidth = (int)(iconHeight * 1.75f);
        int iconX = badgeX + (badgeWidth - iconWidth) / 2;
        int iconY = badgeY + iconPadding;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, SummaryConstants.CROWN_TEXTURE, iconX, iconY,
            0.0f, 0.0f, iconWidth, iconHeight, iconWidth, iconHeight, iconWidth, iconHeight,
            0xFFFFFFFF);
    }

    private static void renderHead(DrawContext context, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, int rowHeight, SummaryDimensions dims) {
        int headX = x + (int)(6 * dims.uiScale);
        int headY = y + (rowHeight - dims.headSize) / 2;

        try {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                PlayerListEntry playerEntry = MinecraftClient.getInstance().getNetworkHandler()
                    .getPlayerList().stream()
                    .filter(entry -> entry.getProfile().name().equals(player.playerName()))
                    .findFirst()
                    .orElse(null);

                if (playerEntry != null) {
                    var skinTextures = playerEntry.getSkinTextures();
                    PlayerSkinDrawer.draw(context, skinTextures, headX, headY, dims.headSize);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void renderName(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, SummaryDimensions dims, float fadeAlpha) {
        int headX = x + (int)(6 * dims.uiScale);
        int textX = headX + dims.headSize + (int)(10 * dims.uiScale);
        int nameAlpha = (int)(fadeAlpha * 255);
        int nameColor = (nameAlpha << 24) | 0xFFFFFF;
        int nameY = y + (int)(4 * dims.uiScale);

        if (dims.isCompactMode) {
            float textScale = Math.max(0.7f, dims.uiScale);
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(textX, nameY);
            matrices.scale(textScale, textScale);
            context.drawText(textRenderer, player.playerName(), 0, 0, nameColor, true);
            matrices.popMatrix();
        } else {
            context.drawText(textRenderer, player.playerName(), textX, nameY, nameColor, true);
        }
    }

    private static void renderStats(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                                    int x, int y, int rowWidth, SummaryDimensions dims, float fadeAlpha, long animationStartTime,
                                    List<AchievementTooltipArea> achievementAreas) {
        int headX = x + (int)(6 * dims.uiScale);
        int textX = headX + dims.headSize + (int)(10 * dims.uiScale);

        BadgeDimensions badgeDims = BadgeDimensions.calculate(dims);
        float animProgress = AnimationHelper.getProgress(animationStartTime);

        int badgeY1 = y + (int)((dims.isCompactMode ? 12 : 16) * dims.uiScale);
        int badgeY2 = badgeY1 + badgeDims.height() + badgeDims.rowSpacing();

        int statsEndX = StatBadgeRenderer.render(context, textRenderer, player, textX, badgeY1, badgeY2,
                                                 badgeDims, animProgress, fadeAlpha);

        if (!player.achievements().isEmpty()) {
            int achievementX = statsEndX + badgeDims.spacing() * 2;
            int maxWidth = x + rowWidth - achievementX - (int)(5 * dims.uiScale);
            AchievementRenderer.render(context, textRenderer, achievementX, badgeY1, badgeY2, player.achievements(),
                                      badgeDims, maxWidth, dims, fadeAlpha, achievementAreas);
        }
    }
}

