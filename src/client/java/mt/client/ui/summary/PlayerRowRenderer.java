package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.util.Identifier;

import java.util.List;

public class PlayerRowRenderer {

    public static void render(DrawContext context, TextRenderer textRenderer, DailySummaryPacket.PlayerDailySummary player,
                              int x, int y, SummaryDimensions dims, float fadeAlpha, long animationStartTime,
                              List<AchievementTooltipArea> achievementAreas) {
        int contentPaddingSides = dims.s(60);
        int rowWidth = dims.panelWidth - contentPaddingSides * 2;
        int rowHeight = dims.playerRowHeight - dims.s(11);

        renderRowBackground(context, player, x, y, rowWidth, rowHeight, fadeAlpha);

        int headSize = (int) (dims.headSize * 0.75f);
        int leftPad = dims.s(24);
        int headColumnWidth = leftPad + headSize + dims.s(19);

        renderHead(context, player, x, y, rowHeight, headSize, leftPad);
        renderNameBadge(context, textRenderer, player, x, y, rowWidth, rowHeight, dims, fadeAlpha);

        int pad = dims.s(8);
        int contentX = x + headColumnWidth;
        int contentY = y + pad;
        int contentH = rowHeight - pad * 2;
        int contentW = rowWidth - headColumnWidth - pad;

        boolean hasAchievements = !player.achievements().isEmpty();
        int statsW = (int) (contentW * 0.65f);
        int achW = contentW - statsW - pad;
        StatBadgeRenderer.render(context, textRenderer, player, contentX, contentY, statsW, contentH, dims, fadeAlpha, animationStartTime);
        if (hasAchievements) {
            AchievementRenderer.render(context, textRenderer, player.achievements(),
                    contentX + statsW + pad, contentY, achW, contentH, dims, fadeAlpha, achievementAreas);
        }
    }

    private static void renderRowBackground(DrawContext context, DailySummaryPacket.PlayerDailySummary player,
                                            int x, int y, int rowWidth, int rowHeight, float fadeAlpha) {
        int texW = SummaryConstants.ROW_TEXTURE_WIDTH;
        int texH = SummaryConstants.ROW_TEXTURE_HEIGHT;
        RenderHelper.ScaledBlit sb = RenderHelper.computeScaledBlit(y, rowWidth, rowHeight, texW, texH);
        int renderX = x + (rowWidth - sb.renderW()) / 2;

        Identifier rowTex = player.isMvp() ? SummaryConstants.getMvpRowTexture() : SummaryConstants.getRowTexture();
        RenderHelper.blitTexture(context, rowTex, renderX, sb.renderY(), sb.renderW(), sb.renderH(), fadeAlpha);
    }

    private static void renderHead(DrawContext context, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, int rowHeight, int headSize, int leftPad) {
        int skinX = x + leftPad;
        int headY = y + (rowHeight - headSize) / 2;
        try {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                MinecraftClient.getInstance().getNetworkHandler()
                        .getPlayerList().stream()
                        .filter(e -> e.getProfile().name().equals(player.playerName()))
                        .findFirst().ifPresent(playerEntry -> PlayerSkinDrawer.draw(context, playerEntry.getSkinTextures(), skinX, headY, headSize));
            }
        } catch (Exception ignored) {}
    }

    private static void renderNameBadge(DrawContext context, TextRenderer textRenderer,
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

        RenderHelper.blitTexture(context, SummaryConstants.getNameBadgeTexture(),
                badgeX, badgeY, renderW, renderH, fadeAlpha);

        float textScale = dims.uiScale * 1.35f;
        String name = player.playerName();
        int nameAlpha = (int) (fadeAlpha * 255);
        int nameColor = (nameAlpha << 24) | 0xFFFFFF;

        int maxTextW = (int) ((renderW - dims.s(6)) / textScale);
        name = RenderUtils.truncateWithEllipsis(textRenderer, name, maxTextW);

        int textW = (int) (textRenderer.getWidth(name) * textScale);
        int textX = badgeX + (renderW - textW) / 2;
        int textY = badgeY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, name, textX, textY, nameColor, textScale, true);
    }
}