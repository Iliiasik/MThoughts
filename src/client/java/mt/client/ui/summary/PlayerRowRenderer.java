package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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
        float scale = Math.min((float) rowHeight / texH, (float) rowWidth / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderX = x + (rowWidth - renderW) / 2;
        int renderY = y + (rowHeight - renderH) / 2;

        int color = (int) (fadeAlpha * 255) << 24 | 0xFFFFFF;
        graphics.pose().pushMatrix();
        graphics.pose().translate(renderX, renderY);
        graphics.pose().scale(scale, scale);
        Identifier rowTex = player.isMvp() ? SummaryConstants.getMvpRowTexture() : SummaryConstants.getRowTexture();
        graphics.blit(RenderPipelines.GUI_TEXTURED, rowTex, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        graphics.pose().popMatrix();
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

        int color = (int) (fadeAlpha * 255) << 24 | 0xFFFFFF;
        graphics.pose().pushMatrix();
        graphics.pose().translate(badgeX, badgeY);
        graphics.pose().scale((float) renderW / texW, (float) renderH / texH);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getNameBadgeTexture(),
                0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        graphics.pose().popMatrix();

        float textScale = dims.uiScale * 1.35f;
        String name = player.playerName();

        int maxTextW = (int) ((renderW - dims.s(6)) / textScale);
        String ellipsis = "...";
        int ellipsisW = font.width(ellipsis);
        if (font.width(name) > maxTextW) {
            while (!name.isEmpty() && font.width(name) + ellipsisW > maxTextW) {
                name = name.substring(0, name.length() - 1);
            }
            name = name + ellipsis;
        }

        int textW = (int) (font.width(name) * textScale);
        int textX = badgeX + (renderW - textW) / 2;
        int textY = badgeY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(graphics, font, name, textX, textY, color, textScale, true);
    }
}