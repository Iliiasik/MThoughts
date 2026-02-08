package mt.client.ui.summary;

import mt.client.MidnightThoughtsClient;
import net.minecraft.util.Identifier;

public class SummaryConstants {
    public static final Identifier BACKGROUND_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/background.png");
    public static final Identifier CROWN_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/crown.png");
    public static final Identifier FRAME_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/frame.png");
    public static final Identifier BADGE_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/badge.png");
    public static final Identifier PAGES_HOLDER_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/pages_holder.png");

    public static final long STAT_ANIMATION_DURATION = 800;
    public static final int MVP_GOLD_COLOR = 0xFFD700;
    public static final int FRAME_TEXTURE_WIDTH = 1000;
    public static final int FRAME_TEXTURE_HEIGHT = 640;
    public static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;
    public static final float BACKGROUND_ASPECT_RATIO = 16.0f / 9.0f;

    public static final int BADGE_TEXTURE_WIDTH = 480;
    public static final int BADGE_TEXTURE_HEIGHT = 160;

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

