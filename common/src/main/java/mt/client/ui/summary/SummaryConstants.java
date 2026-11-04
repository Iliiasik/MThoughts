package mt.client.ui.summary;

import mt.MTConstants;
import net.minecraft.resources.Identifier;

public class SummaryConstants {

    public static final int FRAME_TEXTURE_WIDTH = 260;
    public static final int FRAME_TEXTURE_HEIGHT = 200;

    public static final int INFO_TEXTURE_WIDTH = 220;
    public static final int INFO_TEXTURE_HEIGHT = 160;

    public static final int BADGE_TEXTURE_WIDTH = 32;
    public static final int BADGE_TEXTURE_HEIGHT = 14;
    public static final int BADGE_BORDER = 2;
    public static final int BADGE_INSET = 3;

    public static final int TOOLTIP_TEXTURE_WIDTH = 32;
    public static final int TOOLTIP_TEXTURE_HEIGHT = 16;
    public static final int TOOLTIP_BORDER = 2;

    public static final int STATS_PANEL_TEXTURE_WIDTH = 138;
    public static final int STATS_PANEL_TEXTURE_HEIGHT = 98;

    public static final int ACHIEVEMENT_TEXTURE_SIZE = 24;

    public static final int CHIP_TEXTURE_WIDTH = 64;
    public static final int CHIP_TEXTURE_HEIGHT = 14;
    public static final int CHIP_INSET = 4;

    public static final int BUTTON_TEXTURE_SIZE = 16;

    public static final int SLEEPING_HUD_TEXTURE_WIDTH = 26;
    public static final int SLEEPING_HUD_TEXTURE_HEIGHT = 16;

    private static Identifier themed(String name) {
        return Identifier.fromNamespaceAndPath(MTConstants.MOD_ID,
                "textures/gui/" + ThemeColors.currentTheme() + "/" + name + ".png");
    }

    public static Identifier getFrameTexture() {
        return themed("frame");
    }

    public static Identifier getInfoTexture(boolean mvp) {
        return themed(mvp ? "info_mvp" : "info");
    }

    public static Identifier getBadgeTexture() {
        return themed("badge");
    }

    public static Identifier getStatsPanelTexture() {
        return themed("stats_panel");
    }

    public static Identifier getStatsTableTexture() {
        return themed("stats_table");
    }

    public static Identifier getAchievementTexture() {
        return themed("achievement_badge");
    }

    public static Identifier getChipTexture() {
        return themed("days");
    }

    public static Identifier getTooltipTexture() {
        return themed("tooltip");
    }

    public static Identifier getButtonTexture(boolean next) {
        return themed(next ? "button_right" : "button_left");
    }

    public static Identifier getSleepingHudTexture() {
        return themed("sleeping_hud");
    }
}
