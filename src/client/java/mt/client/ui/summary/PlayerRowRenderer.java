package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.gl.RenderPipelines;
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
        float scale = Math.min((float) rowHeight / texH, (float) rowWidth / texW);
        int renderW = (int) (texW * scale);
        int renderH = (int) (texH * scale);
        int renderX = x + (rowWidth - renderW) / 2;
        int renderY = y + (rowHeight - renderH) / 2;

        int color = (int) (fadeAlpha * 255) << 24 | 0xFFFFFF;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(renderX, renderY);
        context.getMatrices().scale(scale, scale);
        Identifier rowTex = player.isMvp() ? SummaryConstants.getMvpRowTexture() : SummaryConstants.getRowTexture();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, rowTex, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        context.getMatrices().popMatrix();
    }

    private static void renderHead(DrawContext context, DailySummaryPacket.PlayerDailySummary player,
                                   int x, int y, int rowHeight, int headSize, int leftPad) {
        int skinX = x + leftPad;
        int headY = y + (rowHeight - headSize) / 2;
        try {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                PlayerListEntry playerEntry = MinecraftClient.getInstance().getNetworkHandler()
                        .getPlayerList().stream()
                        .filter(e -> e.getProfile().name().equals(player.playerName()))
                        .findFirst().orElse(null);
                if (playerEntry != null) {
                    PlayerSkinDrawer.draw(context, playerEntry.getSkinTextures(), skinX, headY, headSize);
                }
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

        int color = (int) (fadeAlpha * 255) << 24 | 0xFFFFFF;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(badgeX, badgeY);
        context.getMatrices().scale((float) renderW / texW, (float) renderH / texH);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SummaryConstants.getNameBadgeTexture(),
                0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        context.getMatrices().popMatrix();

        float textScale = dims.uiScale * 1.35f;
        String name = player.playerName();

        int maxTextW = (int) ((renderW - dims.s(6)) / textScale);
        String ellipsis = "...";
        int ellipsisW = textRenderer.getWidth(ellipsis);
        if (textRenderer.getWidth(name) > maxTextW) {
            while (name.length() > 0 && textRenderer.getWidth(name) + ellipsisW > maxTextW) {
                name = name.substring(0, name.length() - 1);
            }
            name = name + ellipsis;
        }

        int textW = (int) (textRenderer.getWidth(name) * textScale);
        int textX = badgeX + (renderW - textW) / 2;
        int textY = badgeY + (renderH - (int) (8 * textScale)) / 2;
        RenderUtils.renderScaledText(context, textRenderer, name, textX, textY, color, textScale, true);
    }
}