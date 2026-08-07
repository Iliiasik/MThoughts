package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.ClientConfig;
import net.minecraft.resources.ResourceLocation;

public class SummaryConstants {

    public static final ResourceLocation ICON_BLOCKS = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_blocks.png");
    public static final ResourceLocation ICON_DISTANCE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_distance.png");
    public static final ResourceLocation ICON_SWORD = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_sword.png");
    public static final ResourceLocation ICON_DEATH = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_death.png");
    public static final ResourceLocation ICON_JUMP = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_jump.png");
    public static final ResourceLocation ICON_AXE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/icons/icon_axe.png");

    public static final int ROW_TEXTURE_WIDTH = 320;
    public static final int ROW_TEXTURE_HEIGHT = 64;

    public static final int NAME_BADGE_TEXTURE_WIDTH = 80;
    public static final int NAME_BADGE_TEXTURE_HEIGHT = 14;

    public static final int STAT_BADGE_TEXTURE_WIDTH = 100;
    public static final int STAT_BADGE_TEXTURE_HEIGHT = 14;

    public static final int ACHIEVEMENT_BADGE_TEXTURE_WIDTH = 100;
    public static final int ACHIEVEMENT_BADGE_TEXTURE_HEIGHT = 14;

    public static ResourceLocation getFrameTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/frame.png");
    }

    public static ResourceLocation getBadgeTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/badge.png");
    }

    public static ResourceLocation getPagesHolderTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/pages_holder.png");
    }

    public static ResourceLocation getSleepingHudTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/sleeping_hud.png");
    }

    public static ResourceLocation getRowTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/row.png");
    }

    public static ResourceLocation getMvpRowTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/mvp_row.png");
    }

    public static ResourceLocation getNameBadgeTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/name_badge.png");
    }

    public static ResourceLocation getStatBadgeTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/stat_badge.png");
    }

    public static ResourceLocation getAchievementBadgeTexture() {
        String theme = ClientConfig.getInstance().getEffectiveTheme();
        return ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/" + theme + "/achievement_badge.png");
    }

    public static final long STAT_ANIMATION_DURATION = 800;
    public static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;

    public static final int PAGES_HOLDER_TEXTURE_WIDTH = 380;
    public static final int PAGES_HOLDER_TEXTURE_HEIGHT = 170;

    public static final int PLAYERS_PER_PAGE = 2;
}
