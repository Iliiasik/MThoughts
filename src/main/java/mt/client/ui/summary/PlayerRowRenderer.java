package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.List;

public class PlayerRowRenderer {

    public static void render(GuiGraphics context, Font textRenderer, DailySummaryPacket.PlayerDailySummary player,
                              int x, int y, SummaryDimensions dims, float fadeAlpha, long animationStartTime,
                              List<AchievementTooltipArea> achievementAreas) {

        int contentPaddingSides = dims.s(60);
        int rowWidth = dims.panelWidth - contentPaddingSides * 2;
        int rowHeight = dims.playerRowHeight - dims.s(11);

        renderRowBackground(context, player, x, y, rowWidth, rowHeight, fadeAlpha);

        if (player.isMvp()) {
            renderMvpBadge(context, x, y, rowWidth, dims, fadeAlpha);
        }

        int headSize = (int)(dims.headSize * 0.75f);
        int leftPad = dims.s(24);
        int headColumnWidth = leftPad + headSize + dims.s(19);

        renderHead(context, player, x, y, rowHeight, headSize, leftPad, fadeAlpha);
        renderNameBadge(context, textRenderer, player, x, y, rowWidth, rowHeight, dims, fadeAlpha);

        int pad = dims.s(8);
        int contentX = x + headColumnWidth;
        int contentY = y + pad;
        int contentH = rowHeight - pad * 2;
        int contentW = rowWidth - headColumnWidth - pad;

        boolean hasAchievements = !player.achievements().isEmpty();
        int statsW = (int)(contentW * 0.65f);
        int achW = contentW - statsW - pad;
        StatBadgeRenderer.render(context, textRenderer, player, contentX, contentY, statsW, contentH, dims, fadeAlpha, animationStartTime);
        if (hasAchievements) {
            AchievementRenderer.render(context, textRenderer, player.achievements(),
                    contentX + statsW + pad, contentY, achW, contentH, dims, fadeAlpha, achievementAreas);
        }
    }

    private static void renderRowBackground(GuiGraphics context, DailySummaryPacket.PlayerDailySummary player,
                                            int x, int y, int rowWidth, int rowHeight, float fadeAlpha) {
        int texW = SummaryConstants.ROW_TEXTURE_WIDTH;
        int texH = SummaryConstants.ROW_TEXTURE_HEIGHT;
        float scale = Math.min((float) rowHeight / texH, (float) rowWidth / texW);
        int renderW = (int)(texW * scale);
        int renderH = (int)(texH * scale);
        int renderX = x + (rowWidth - renderW) / 2;
        int renderY = y + (rowHeight - renderH) / 2;

        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(renderX, renderY);
        context.pose().scale(scale, scale);
        Identifier rowTex = player.isMvp() ? SummaryConstants.getMvpRowTexture() : SummaryConstants.getRowTexture();
        context.blit(RenderPipelines.GUI_TEXTURED, rowTex, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH, color);
        context.pose().popMatrix();
    }

    private static void renderMvpBadge(GuiGraphics context, int x, int y, int rowWidth, SummaryDimensions dims, float fadeAlpha) {
        int crownHeight = dims.s(24);
        int crownWidth = (int)(crownHeight * 1.75f);
        int mvpBadgeX = x + rowWidth - crownWidth;
        int mvpBadgeY = y - crownHeight / 2;

        int badgeAlpha = (int)(fadeAlpha * 240);
        context.fill(mvpBadgeX - 1, mvpBadgeY - 1, mvpBadgeX + crownWidth + 1, mvpBadgeY + crownHeight + 1,
                (badgeAlpha << 24) | SummaryConstants.MVP_GOLD_COLOR);
        context.fill(mvpBadgeX, mvpBadgeY, mvpBadgeX + crownWidth, mvpBadgeY + crownHeight,
                (badgeAlpha << 24) | 0x8b7320);

        int iconPadding = dims.s(3);
        int iconHeight = crownHeight - iconPadding * 2;
        int iconWidth = (int)(iconHeight * 1.75f);
        int iconX = mvpBadgeX + (crownWidth - iconWidth) / 2;
        int iconY = mvpBadgeY + iconPadding;
        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.CROWN_TEXTURE,
                iconX, iconY, 0.0f, 0.0f, iconWidth, iconHeight, iconWidth, iconHeight, color);
    }

    private static void renderHead(GuiGraphics context, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, int rowHeight, int headSize, int leftPad, float fadeAlpha) {
        int skinX = x + leftPad;
        int headY = y + (rowHeight - headSize) / 2;
        try {
            if (Minecraft.getInstance().getConnection() != null) {
                PlayerInfo playerEntry = Minecraft.getInstance().getConnection()
                        .getOnlinePlayers().stream()
                        .filter(e -> e.getProfile().name().equals(player.playerName()))
                        .findFirst().orElse(null);
                if (playerEntry != null) {
                    Identifier skin = playerEntry.getSkin().body().texturePath();
                    int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
                    context.blit(RenderPipelines.GUI_TEXTURED, skin, skinX, headY, 8.0f, 8.0f, headSize, headSize, 8, 8, 64, 64, color);
                    context.blit(RenderPipelines.GUI_TEXTURED, skin, skinX, headY, 40.0f, 8.0f, headSize, headSize, 8, 8, 64, 64, color);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void renderNameBadge(GuiGraphics context, Font textRenderer,
                                        DailySummaryPacket.PlayerDailySummary player,
                                        int x, int y, int rowWidth, int rowHeight,
                                        SummaryDimensions dims, float fadeAlpha) {
        int texW = SummaryConstants.NAME_BADGE_TEXTURE_WIDTH;
        int texH = SummaryConstants.NAME_BADGE_TEXTURE_HEIGHT;

        float scale = Math.min(1.0f, (float) rowHeight * 0.18f / texH);
        int renderW = (int)(texW * scale);
        int renderH = (int)(texH * scale);

        int badgeX = x + (rowWidth - renderW) / 2;
        int badgeY = y - renderH / 2;

        int color = ARGB.colorFromFloat(fadeAlpha, 1.0f, 1.0f, 1.0f);
        context.pose().pushMatrix();
        context.pose().translate(badgeX, badgeY);
        context.pose().scale((float) renderW / texW, (float) renderH / texH);
        context.blit(RenderPipelines.GUI_TEXTURED, SummaryConstants.getNameBadgeTexture(),
                0, 0, 0.0f, 0.0f, texW, texH, texW, texH, color);
        context.pose().popMatrix();

        float textScale = Math.min(0.9f, scale * 1.2f);
        String name = player.playerName();
        int textW = (int)(textRenderer.width(name) * textScale);
        int textX = badgeX + (renderW - textW) / 2;
        int textY = badgeY + (renderH - (int)(8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, name, textX, textY, color, textScale, true);
    }
}