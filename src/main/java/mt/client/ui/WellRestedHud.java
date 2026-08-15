package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.manager.WellRestedClientState;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class WellRestedHud {
    private static final ResourceLocation SCALE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/hud/scale.png");
    private static final ResourceLocation FILL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/hud/fill.png");
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/hud/well_rested.png");
    private static final ResourceLocation MVP_ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/hud/mvp.png");

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

    private static final int MARGIN_SIDE = 4;
    private static final int MARGIN_BOTTOM = 4;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};
    private static final int BLINK_THRESHOLD_TICKS = 200;

    public static boolean isActive() {
        return WellRestedClientState.isActive()
                && !MidnightThoughtsConfig.getInstance().isHideWellRestedHud();
    }

    public static boolean isBarPosition() {
        return MidnightThoughtsConfig.HUD_POSITION_BAR
                .equals(MidnightThoughtsConfig.getInstance().getWellRestedHudPosition());
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
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
        ResourceLocation activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.width(roman);

        float scaleAlpha = 1.0f;
        if (ticksRemaining <= BLINK_THRESHOLD_TICKS && ticksRemaining > 0) {
            float sin = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 3.0f);
            scaleAlpha = 0.4f + 0.6f * (sin * 0.5f + 0.5f);
        }

        String position = MidnightThoughtsConfig.getInstance().getWellRestedHudPosition();
        boolean bar = MidnightThoughtsConfig.HUD_POSITION_BAR.equals(position);
        int romanSpace = roman.isEmpty() ? 0 : romanWidth + GAP;
        int totalRight = screenWidth / 2 + 91;

        int iconX;
        int barY;
        if (bar) {
            iconX = totalRight - TOTAL_GUI_W;
            barY = screenHeight - 32 - BAR_GUI_H - 10;
        } else {
            barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;
            iconX = MidnightThoughtsConfig.HUD_POSITION_RIGHT.equals(position)
                    ? screenWidth - MARGIN_SIDE - (ICON_GUI_SIZE + GAP + romanSpace + TEX_SCALE_W)
                    : MARGIN_SIDE;
        }

        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + romanSpace;
        int barGuiW = bar ? totalRight - barX : TEX_SCALE_W;

        blitTinted(graphics, activeIcon, iconX, barY, ICON_GUI_SIZE, ICON_GUI_SIZE,
                TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, scaleAlpha);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            int textColor = (Math.round(scaleAlpha * 255f) << 24) | 0x00FFD966;
            graphics.drawString(font, roman, romanX, romanY, textColor, true);
        }

        if (barGuiW > 0) {
            blitTinted(graphics, SCALE_TEXTURE, barX, barY, barGuiW, BAR_GUI_H,
                    TEX_SCALE_W, TEX_SCALE_H, TEX_SCALE_W, TEX_SCALE_H, scaleAlpha);

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    int texFillW = Math.round(visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW));
                    blitTinted(graphics, FILL_TEXTURE, fillGuiX, fillGuiY, visibleFillGuiW, TEX_FILL_H,
                            texFillW, TEX_FILL_H, TEX_FILL_W, TEX_FILL_H, scaleAlpha);
                }
            }
        }

        RenderSystem.disableBlend();
    }

    private static void blitTinted(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                                   int guiW, int guiH, int srcW, int srcH, int texW, int texH, float alpha) {
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, x, y, guiW, guiH, 0.0f, 0.0f, srcW, srcH, texW, texH);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
