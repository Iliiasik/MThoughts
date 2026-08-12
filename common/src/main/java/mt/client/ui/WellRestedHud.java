package mt.client.ui;

import mt.MTConstants;
import mt.client.manager.WellRestedClientState;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

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

    private static final int MARGIN_LEFT = 4;
    private static final int MARGIN_BOTTOM = 4;
    private static final int TOTAL_GUI_W = 81;

    public static boolean isActive() {
        return WellRestedClientState.isActive()
                && !MidnightThoughtsConfig.getInstance().isHideWellRestedHud();
    }

    public static boolean isPrimaryPosition() {
        return "primary".equals(MidnightThoughtsConfig.getInstance().getWellRestedHudPosition());
    }

    public static void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;
        Matrix3x2fStack matrices = context.pose();

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

        boolean primary = isPrimaryPosition();
        int totalRight = screenWidth / 2 + 91;

        int iconX;
        int barY;
        if (primary) {
            iconX = totalRight - TOTAL_GUI_W;
            barY = screenHeight - 32 - BAR_GUI_H - 10;
        } else {
            iconX = MARGIN_LEFT;
            barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;
        }

        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + (roman.isEmpty() ? 0 : romanWidth + GAP);
        int barGuiW = primary ? totalRight - barX : TEX_SCALE_W;

        blitScaled(context, activeIcon, iconX, barY, ICON_GUI_SIZE, ICON_GUI_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            context.text(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        if (barGuiW > 0) {
            blitScaled(context, SCALE_TEXTURE, barX, barY, barGuiW, BAR_GUI_H, TEX_SCALE_W, TEX_SCALE_H);

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    float texFillW = visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW);
                    matrices.pushMatrix();
                    matrices.translate(fillGuiX, fillGuiY);
                    matrices.scale((float) fillGuiW / TEX_FILL_W, 1f);
                    context.blit(RenderPipelines.GUI_TEXTURED, FILL_TEXTURE, 0, 0, 0.0f, 0.0f, Math.round(texFillW), TEX_FILL_H, TEX_FILL_W, TEX_FILL_H);
                    matrices.popMatrix();
                }
            }
        }
    }

    private static void blitScaled(GuiGraphicsExtractor context, Identifier texture,
                                   int x, int y, int guiW, int guiH, int texW, int texH) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale((float) guiW / texW, (float) guiH / texH);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH);
        matrices.popMatrix();
    }
}
