package mt.client.ui.summary;

import mt.client.ui.engine.RenderHelper;
import mt.client.ui.engine.RenderUtils;
import mt.client.util.NumberFormatter;
import mt.integration.mca.McaCompat;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class CardRenderer {

    private CardRenderer() {}

    public static void render(GuiGraphicsExtractor context, Font font, DailySummaryPacket.PlayerDailySummary player,
                              SummaryLayout layout, float fadeAlpha, float countProgress, List<HoverArea> areas) {
        renderHead(context, player, layout, fadeAlpha);
        addRewardsArea(player, layout, areas);
        renderDaysChip(context, font, player, layout, fadeAlpha, areas);
        StatsPanelRenderer.render(context, font, player, layout, fadeAlpha, countProgress, areas);
        AchievementRenderer.render(context, player.achievements(), layout, fadeAlpha, areas);
    }

    private static void renderHead(GuiGraphicsExtractor context, DailySummaryPacket.PlayerDailySummary player,
                                   SummaryLayout layout, float fadeAlpha) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        String name = player.playerName();
        if (name == null) return;

        PlayerInfo entry = null;
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (name.equals(info.getProfile().name())) {
                entry = info;
                break;
            }
        }
        if (entry == null) return;

        int size = layout.s(SummaryLayout.HEAD);
        int x = layout.contentX();
        int y = layout.columnsY();

        Identifier skin = McaCompat.headTexture(entry.getProfile().id());
        if (skin == null) skin = entry.getSkin().body().texturePath();
        PlayerFaceExtractor.extractRenderState(context, skin, x, y, size, true, false, RenderHelper.white(fadeAlpha));
    }

    private static void addRewardsArea(DailySummaryPacket.PlayerDailySummary player, SummaryLayout layout,
                                       List<HoverArea> areas) {
        List<String> rewards = RewardsTooltip.lines(player);
        if (rewards.isEmpty()) return;
        int size = layout.s(SummaryLayout.HEAD);
        areas.add(new HoverArea(layout.contentX(), layout.columnsY(), size, size, rewards));
    }

    private static void renderDaysChip(GuiGraphicsExtractor context, Font font,
                                         DailySummaryPacket.PlayerDailySummary player,
                                         SummaryLayout layout, float fadeAlpha, List<HoverArea> areas) {
        String text = Component.translatable("midnightthoughts.summary.days",
                NumberFormatter.formatLargeNumber(player.day())).getString();

        int x = layout.contentX();
        int y = layout.chipY();
        int width = layout.chipColumnWidth();
        int height = layout.s(SummaryConstants.CHIP_TEXTURE_HEIGHT);
        int inset = layout.s(SummaryConstants.CHIP_INSET);
        float textScale = layout.textScale();

        RenderHelper.blitTexture(context, SummaryConstants.getChipTexture(), x, y, width, height, fadeAlpha);

        String fitted = RenderUtils.truncateWithEllipsis(font, text,
                Math.max(1, Math.round((width - inset * 2) / textScale)));
        int textWidth = RenderUtils.scaledWidth(font, fitted, textScale);
        ThemeColors.TextStyle style = ThemeColors.current().days();

        RenderUtils.renderScaledText(context, font, fitted,
                RenderUtils.centeredX(x, width, textWidth),
                RenderUtils.centeredTextY(y, height, textScale, style.shadow()),
                style.argb(fadeAlpha), textScale, style.shadow());

        List<String> season = SeasonChip.tooltipLines();
        if (!season.isEmpty()) {
            areas.add(new HoverArea(x, y, width, height, season));
        }
    }
}
