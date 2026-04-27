package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import net.minecraft.resources.Identifier;

public class SummaryConstants {

    public static final Identifier ICON_BLOCKS = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_blocks.png");
    public static final Identifier ICON_DISTANCE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_distance.png");
    public static final Identifier ICON_SWORD = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_sword.png");
    public static final Identifier ICON_DEATH = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_death.png");
    public static final Identifier ICON_JUMP = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_jump.png");
    public static final Identifier ICON_AXE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_axe.png");

    public static final int ROW_TEXTURE_WIDTH = 320;
    public static final int ROW_TEXTURE_HEIGHT = 64;

    public static final int NAME_BADGE_TEXTURE_WIDTH = 80;
    public static final int NAME_BADGE_TEXTURE_HEIGHT = 14;

    public static final int STAT_BADGE_TEXTURE_WIDTH = 100;
    public static final int STAT_BADGE_TEXTURE_HEIGHT = 14;

    public static final int ACHIEVEMENT_BADGE_TEXTURE_WIDTH = 100;
    public static final int ACHIEVEMENT_BADGE_TEXTURE_HEIGHT = 14;

    public static final long STAT_ANIMATION_DURATION = 800;
    public static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;

    public static final int FRAME_TEXTURE_WIDTH = 1000;
    public static final int FRAME_TEXTURE_HEIGHT = 640;

    public static final int BADGE_TEXTURE_WIDTH = 480;
    public static final int BADGE_TEXTURE_HEIGHT = 160;

    public static final int PAGES_HOLDER_TEXTURE_WIDTH = 380;
    public static final int PAGES_HOLDER_TEXTURE_HEIGHT = 170;

    public static final int NORMAL_MODE_PLAYERS_PER_PAGE = 2;

    public static Identifier getFrameTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/frame.png");
    }

    public static Identifier getBadgeTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/badge.png");
    }

    public static Identifier getPagesHolderTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/pages_holder.png");
    }

    public static Identifier getRowTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/row.png");
    }

    public static Identifier getMvpRowTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/mvp_row.png");
    }

    public static Identifier getNameBadgeTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/name_badge.png");
    }

    public static Identifier getStatBadgeTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/stat_badge.png");
    }

    public static Identifier getAchievementBadgeTexture() {
        String theme = mt.client.config.ClientConfig.getInstance().getEffectiveTheme();
        return Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/achievement_badge.png");
    }
}