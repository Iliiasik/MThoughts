package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.resources.ResourceLocation;

public class SummaryConstants {

    public static final ResourceLocation ICON_BLOCKS = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_blocks.png");
    public static final ResourceLocation ICON_DISTANCE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_distance.png");
    public static final ResourceLocation ICON_SWORD = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_sword.png");
    public static final ResourceLocation ICON_DEATH = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_death.png");
    public static final ResourceLocation ICON_JUMP = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_jump.png");
    public static final ResourceLocation ICON_AXE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icon_axe.png");

    public static final int ROW_TEXTURE_WIDTH = 320;
    public static final int ROW_TEXTURE_HEIGHT = 64;

    public static final int NAME_BADGE_TEXTURE_WIDTH = 80;
    public static final int NAME_BADGE_TEXTURE_HEIGHT = 14;

    public static final int STAT_BADGE_TEXTURE_WIDTH = 100;
    public static final int STAT_BADGE_TEXTURE_HEIGHT = 14;

    public static final int ACHIEVEMENT_BADGE_TEXTURE_WIDTH = 100;
    public static final int ACHIEVEMENT_BADGE_TEXTURE_HEIGHT = 14;

    public static ResourceLocation getFrameTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/frame.png");
    }

    public static ResourceLocation getBadgeTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/badge.png");
    }

    public static ResourceLocation getPagesHolderTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/pages_holder.png");
    }

    public static ResourceLocation getSleepingHudTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/sleeping_hud.png");
    }

    public static ResourceLocation getRowTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/row.png");
    }

    public static ResourceLocation getMvpRowTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/mvp_row.png");
    }

    public static ResourceLocation getNameBadgeTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/name_badge.png");
    }

    public static ResourceLocation getStatBadgeTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/stat_badge.png");
    }

    public static ResourceLocation getAchievementBadgeTexture() {
        String theme = MidnightThoughtsConfig.getInstance().getUiTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/achievement_badge.png");
    }

    public static final long STAT_ANIMATION_DURATION = 800;
    public static final int MVP_GOLD_COLOR = 0xFFD700;
    public static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;

    public static final int FRAME_TEXTURE_WIDTH = 1000;
    public static final int FRAME_TEXTURE_HEIGHT = 640;

    public static final int BADGE_TEXTURE_WIDTH = 480;
    public static final int BADGE_TEXTURE_HEIGHT = 160;

    public static final int PAGES_HOLDER_TEXTURE_WIDTH = 380;
    public static final int PAGES_HOLDER_TEXTURE_HEIGHT = 170;

    public static final int NORMAL_MODE_PLAYERS_PER_PAGE = 2;
}