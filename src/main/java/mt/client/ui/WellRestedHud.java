package mt.client.ui;

import mt.client.MidnightThoughtsClient;
import mt.client.manager.WellRestedClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class WellRestedHud {
    private static final Identifier SCALE_TEXTURE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/scale.png");
    private static final Identifier FILL_TEXTURE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/fill.png");
    private static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/well_rested.png");
    private static final Identifier MVP_ICON_TEXTURE = Identifier.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/mvp.png");

    private static final int TEX_SCALE_W = 54;
    private static final int TEX_SCALE_H = 9;
    private static final int TEX_FILL_W = 50;
    private static final int TEX_FILL_H = 5;
    private static final int TEX_ICON_SIZE = 9;
    private static final int TOTAL_GUI_W = 81;
    public static final int BAR_GUI_H = 9;
    private static final int ICON_GUI_SIZE = 9;
    private static final int GAP = 1;
    private static final int FILL_INSET = 2;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};
    private static final int BLINK_THRESHOLD_TICKS = 200;

    public static boolean isActive() {
        return WellRestedClientState.isActive();
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (!WellRestedClientState.isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;

        int level = WellRestedClientState.getLevel();
        int ticksRemaining = WellRestedClientState.getTicksRemaining();
        int totalTicks = WellRestedClientState.getTotalTicks();

        float progress = totalTicks > 0 ? (float) ticksRemaining / totalTicks : 0f;

        int totalRight = screenWidth / 2 + 91;
        int totalLeft = totalRight - TOTAL_GUI_W;
        int barY = screenHeight - 32 - BAR_GUI_H - 10;

        float scaleAlpha = 1.0f;
        if (ticksRemaining <= BLINK_THRESHOLD_TICKS && ticksRemaining > 0) {
            float sin = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 3.0f);
            scaleAlpha = 0.4f + 0.6f * (sin * 0.5f + 0.5f);
        }

        int white = ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f);
        int scaleColor = ARGB.colorFromFloat(scaleAlpha, 1.0f, 1.0f, 1.0f);

        boolean isMvp = WellRestedClientState.isMvp();
        Identifier activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.width(roman);

        int iconX = totalLeft;
        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + (roman.isEmpty() ? 0 : romanWidth + GAP);
        int barGuiW = totalRight - barX;

        graphics.pose().pushMatrix();
        graphics.pose().translate(iconX, barY);
        graphics.pose().scale((float) ICON_GUI_SIZE / TEX_ICON_SIZE, (float) ICON_GUI_SIZE / TEX_ICON_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, activeIcon,
                0, 0, 0.0f, 0.0f,
                TEX_ICON_SIZE, TEX_ICON_SIZE,
                TEX_ICON_SIZE, TEX_ICON_SIZE, white);
        graphics.pose().popMatrix();

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            graphics.drawString(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        if (barGuiW > 0) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(barX, barY);
            graphics.pose().scale((float) barGuiW / TEX_SCALE_W, (float) BAR_GUI_H / TEX_SCALE_H);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SCALE_TEXTURE,
                    0, 0, 0.0f, 0.0f,
                    TEX_SCALE_W, TEX_SCALE_H,
                    TEX_SCALE_W, TEX_SCALE_H, scaleColor);
            graphics.pose().popMatrix();

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    float texFillW = visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW);
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(fillGuiX, fillGuiY);
                    graphics.pose().scale((float) fillGuiW / TEX_FILL_W, (float) TEX_FILL_H / TEX_FILL_H);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, FILL_TEXTURE,
                            0, 0, 0.0f, 0.0f,
                            Math.round(texFillW), TEX_FILL_H,
                            TEX_FILL_W, TEX_FILL_H, white);
                    graphics.pose().popMatrix();
                }
            }
        }
    }
}