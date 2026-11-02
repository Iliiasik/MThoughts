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
    private static final int BAR_GUI_H = 9;
    private static final int ICON_GUI_SIZE = 9;
    private static final int GAP = 1;
    private static final int FILL_INSET = 2;

    private static final int MARGIN_SIDE = 4;
    private static final int MARGIN_BOTTOM = 4;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    public static boolean isActive() {
        return WellRestedClientState.isActive()
                && !MidnightThoughtsConfig.getInstance().isHideWellRestedHud();
    }

    public static int airBubbleShift() {
        return isActive() && isBarPosition() ? BAR_GUI_H + 3 : 0;
    }

    private static boolean isBarPosition() {
        return MidnightThoughtsConfig.HUD_POSITION_BAR
                .equals(MidnightThoughtsConfig.getInstance().getWellRestedHudPosition());
    }

    public static void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
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

        String position = MidnightThoughtsConfig.getInstance().getWellRestedHudPosition();
        int romanSpace = roman.isEmpty() ? 0 : romanWidth + GAP;
        int totalRight = screenWidth / 2 + 91;

        int hudWidth = ICON_GUI_SIZE + GAP + romanSpace + TEX_SCALE_W;

        int iconX;
        int barY;
        if (isBarPosition()) {
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

        blit(graphics, activeIcon, iconX, barY, ICON_GUI_SIZE, ICON_GUI_SIZE,
                TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            graphics.text(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        blit(graphics, SCALE_TEXTURE, barX, barY, TEX_SCALE_W, BAR_GUI_H,
                TEX_SCALE_W, TEX_SCALE_H, TEX_SCALE_W, TEX_SCALE_H);

        if (progress > 0f) {
            int fillGuiX = barX + FILL_INSET;
            int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
            int visibleFillW = Math.round(TEX_FILL_W * progress);
            if (visibleFillW > 0) {
                blit(graphics, FILL_TEXTURE, fillGuiX, fillGuiY, visibleFillW, TEX_FILL_H,
                        visibleFillW, TEX_FILL_H, TEX_FILL_W, TEX_FILL_H);
            }
        }

    }

    private static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y,
                             int guiW, int guiH, int srcW, int srcH, int texW, int texH) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, guiW, guiH, srcW, srcH, texW, texH);
    }
}
