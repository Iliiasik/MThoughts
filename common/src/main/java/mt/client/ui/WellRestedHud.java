package mt.client.ui;

import mt.MTConstants;
import mt.client.manager.WellRestedClientState;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class WellRestedHud {
    private static final Identifier SCALE_TEXTURE = Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "textures/gui/shared/hud/scale.png");
    private static final Identifier FILL_TEXTURE = Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "textures/gui/shared/hud/fill.png");
    private static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "textures/gui/shared/hud/well_rested.png");
    private static final Identifier MVP_ICON_TEXTURE = Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "textures/gui/shared/hud/mvp.png");

    private static final int TEX_SCALE_W = 54;
    private static final int TEX_SCALE_H = 9;
    private static final int TEX_FILL_W = 50;
    private static final int TEX_FILL_H = 5;
    private static final int TEX_ICON_SIZE = 9;
    public static final int BAR_GUI_H = 9;
    private static final int ICON_GUI_SIZE = 9;
    private static final int GAP = 1;
    private static final int FILL_INSET = 2;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};
    private static final int BLINK_THRESHOLD_TICKS = 200;

    private static final int MARGIN_SIDE = 4;
    private static final int MARGIN_BOTTOM = 4;

    public static boolean isActive() {
        return WellRestedClientState.isActive()
                && !MidnightThoughtsConfig.getInstance().isHideWellRestedHud();
    }

    public static boolean isBarPosition() {
        return MidnightThoughtsConfig.HUD_POSITION_BAR
                .equals(MidnightThoughtsConfig.getInstance().getWellRestedHudPosition());
    }

    public static void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;

        int level = WellRestedClientState.getLevel();
        int ticksRemaining = WellRestedClientState.getTicksRemaining();
        int totalTicks = WellRestedClientState.getTotalTicks();

        float progress = totalTicks > 0
                ? Math.clamp((float) ticksRemaining / totalTicks, 0f, 1f)
                : 0f;

        boolean isMvp = WellRestedClientState.isMvp();
        Identifier activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.width(roman);

        float scaleAlpha = 1.0f;
        if (ticksRemaining <= BLINK_THRESHOLD_TICKS && ticksRemaining > 0) {
            float sin = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 3.0f);
            scaleAlpha = 0.4f + 0.6f * (sin * 0.5f + 0.5f);
        }

        int alpha = Math.round(scaleAlpha * 255f) << 24;
        int scaleColor = alpha | 0x00FFFFFF;

        String position = MidnightThoughtsConfig.getInstance().getWellRestedHudPosition();
        boolean bar = MidnightThoughtsConfig.HUD_POSITION_BAR.equals(position);
        int romanSpace = roman.isEmpty() ? 0 : romanWidth + GAP;
        int totalRight = screenWidth / 2 + 91;

        int hudWidth = ICON_GUI_SIZE + GAP + romanSpace + TEX_SCALE_W;

        int iconX;
        int barY;
        if (bar) {
            iconX = totalRight - hudWidth;
            barY = screenHeight - 32 - BAR_GUI_H - 10;
        } else {
            barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;
            iconX = MidnightThoughtsConfig.HUD_POSITION_RIGHT.equals(position)
                    ? screenWidth - MARGIN_SIDE - hudWidth
                    : MARGIN_SIDE;
        }

        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + romanSpace;

        context.blit(RenderPipelines.GUI_TEXTURED, activeIcon, iconX, barY, 0.0f, 0.0f,
                ICON_GUI_SIZE, ICON_GUI_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, scaleColor);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            context.text(font, roman, romanX, romanY, alpha | 0x00FFD966, true);
        }

        context.blit(RenderPipelines.GUI_TEXTURED, SCALE_TEXTURE, barX, barY, 0.0f, 0.0f,
                TEX_SCALE_W, BAR_GUI_H, TEX_SCALE_W, TEX_SCALE_H, scaleColor);

        if (progress > 0f) {
            int fillGuiX = barX + FILL_INSET;
            int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
            int visibleFillW = Math.round(TEX_FILL_W * progress);
            if (visibleFillW > 0) {
                context.blit(RenderPipelines.GUI_TEXTURED, FILL_TEXTURE, fillGuiX, fillGuiY, 0.0f, 0.0f,
                        visibleFillW, TEX_FILL_H, TEX_FILL_W, TEX_FILL_H, scaleColor);
            }
        }
    }
}
