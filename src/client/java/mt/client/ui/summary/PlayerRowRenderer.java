package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

public class PlayerRowRenderer {

    public static void render(GuiGraphicsExtractor graphics, Font font, DailySummaryPacket.PlayerDailySummary player,
                              int x, int y, SummaryDimensions dims, float fadeAlpha, long animationStartTime,
                              List<AchievementTooltipArea> achievementAreas) {
        int contentPaddingSides = dims.s(60);
        int rowWidth = dims.panelWidth - contentPaddingSides * 2;
        int rowHeight = dims.playerRowHeight - dims.s(11);

        renderRowBackground(graphics, player, x, y, rowWidth, rowHeight, fadeAlpha);

        int headSize = (int) (dims.headSize * 0.75f);
        int leftPad = dims.s(24);
        int headColumnWidth = leftPad + headSize + dims.s(19);

        renderHead(graphics, player, x, y, rowHeight, headSize, leftPad);
        renderNameBadge(graphics, font, player, x, y, rowWidth, rowHeight, dims, fadeAlpha);

        int pad = dims.s(8);
        int contentX = x + headColumnWidth;
        int contentY = y + pad;
        int contentH = rowHeight - pad * 2;
        int contentW = rowWidth - headColumnWidth - pad;

        boolean hasAchievements = !player.achievements().isEmpty();
        int statsW = (int) (contentW * 0.65f);
        int achW = contentW - statsW - pad;
        StatBadgeRenderer.render(graphics, font, player, contentX, contentY, statsW, contentH, dims, fadeAlpha, animationStartTime);
        if (hasAchievements) {
            AchievementRenderer.render(graphics, font, player.achievements(),
                    contentX + statsW + pad, contentY, achW, contentH, dims, fadeAlpha, achievementAreas);
        }
    }

    private static void renderRowBackground(GuiGraphicsExtractor graphics, DailySummaryPacket.PlayerDailySummary player,
                                            int x, int y, int rowWidth, int rowHeight, float fadeAlpha) {
        int texW = SummaryConstants.ROW_TEXTURE_WIDTH;
        int texH = SummaryConstants.ROW_TEXTURE_HEIGHT;
        RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(y, rowWidth, rowHeight, texW, texH);
        int renderX = x + (rowWidth - sb.renderW()) / 2;

        Identifier rowTex = player.isMvp() ? SummaryConstants.getMvpRowTexture() : SummaryConstants.getRowTexture();
        RenderHelper.blitTexture(graphics, rowTex, renderX, sb.renderY(), sb.renderW(), sb.renderH(), fadeAlpha);
    }

    private static void renderHead(GuiGraphicsExtractor graphics, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, int rowHeight, int headSize, int leftPad) {
        int skinX = x + leftPad;
        int headY = y + (rowHeight - headSize) / 2;
        try {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection()
                        .getOnlinePlayers().stream()
                        .filter(e -> e.getProfile().name().equals(player.playerName()))
                        .findFirst().ifPresent(playerEntry -> PlayerFaceExtractor.extractRenderState(graphics, playerEntry.getSkin(), skinX, headY, headSize));
            }
        } catch (Exception ignored) {}
    }

    private static void renderNameBadge(GuiGraphicsExtractor graphics, Font font,
                                        DailySummaryPacket.PlayerDailySummary player,
                                        int x, int y, int rowWidth, int rowHeight,
                                        SummaryDimensions dims, float fadeAlpha) {
        int texW = SummaryConstants.NAME_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.NAME_BADGE_TEXTURE_HEIGHT;

        float scale = Math.min(dims.uiScale * 1.5f, (float) rowHeight * 0.27f / texH);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int badgeX = x + (rowWidth - renderW) / 2;
        int badgeY = y - renderH / 2;

        RenderHelper.blitTexture(graphics, SummaryConstants.getNameBadgeTexture(),
                badgeX, badgeY, renderW, renderH, fadeAlpha);

        float textScale = dims.uiScale * 1.35f;
        String name = player.playerName();
        int nameAlpha = (int) (fadeAlpha * 255);
        int nameColor = (nameAlpha << 24) | 0xFFFFFF;

        int maxTextW = (int) ((renderW - dims.s(6)) / textScale);
        name = RenderUtils.truncateWithEllipsis(font, name, maxTextW);

        int textW = (int) (font.width(name) * textScale);
        int textX = badgeX + (renderW - textW) / 2;
        int textY = badgeY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(graphics, font, name, textX, textY, nameColor, textScale, true);
    }
}