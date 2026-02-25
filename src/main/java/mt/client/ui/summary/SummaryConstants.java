package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.resources.ResourceLocation;

public class SummaryConstants {
    public static final ResourceLocation CROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/crown.png");

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

    public static final long STAT_ANIMATION_DURATION = 800;
    public static final int MVP_GOLD_COLOR = 0xFFD700;
    public static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;
    public static final float BACKGROUND_ASPECT_RATIO = 16.0f / 9.0f;

    public static final int PAGES_HOLDER_TEXTURE_WIDTH = 380;
    public static final int PAGES_HOLDER_TEXTURE_HEIGHT = 170;

    public static final int COMPACT_MODE_HEIGHT_THRESHOLD = 350;
    public static final int COMPACT_MODE_WIDTH_THRESHOLD = 500;
    public static final int COMPACT_MODE_PLAYERS_PER_PAGE = 3;
    public static final int NORMAL_MODE_PLAYERS_PER_PAGE = 4;

    public static final int FRAME_CONTENT_PADDING_TOP = 70;
    public static final int FRAME_CONTENT_PADDING_BOTTOM = 50;
    public static final int FRAME_CONTENT_PADDING_SIDES = 80;
}

