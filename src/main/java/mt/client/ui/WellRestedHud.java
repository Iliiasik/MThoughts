package mt.client.ui;

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
    public static final int BAR_GUI_H = 9;
    private static final int ICON_GUI_SIZE = 9;
    private static final int GAP = 1;
    private static final int FILL_INSET = 2;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    private static final int MARGIN_LEFT = 4;
    private static final int MARGIN_BOTTOM = 4;

    public static void render(GuiGraphics graphics, int screenHeight) {
        if (!WellRestedClientState.isActive() || MidnightThoughtsConfig.getInstance().isHideWellRestedHud()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;

        int level = WellRestedClientState.getLevel();
        int ticksRemaining = WellRestedClientState.getTicksRemaining();
        int totalTicks = WellRestedClientState.getTotalTicks();

        float progress = totalTicks > 0
                ? Math.max(0f, Math.min(1f, (float) ticksRemaining / totalTicks))
                : 0f;

        int barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;

        boolean isMvp = WellRestedClientState.isMvp();
        ResourceLocation activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.width(roman);

        int iconX = MARGIN_LEFT;
        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + (roman.isEmpty() ? 0 : romanWidth + GAP);
        int barEndX = barX + TEX_SCALE_W;
        int barGuiW = barEndX - barX;

        graphics.setColor(1f, 1f, 1f, 1f);
        blitScaled(graphics, activeIcon, iconX, barY, ICON_GUI_SIZE, ICON_GUI_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE);
        graphics.setColor(1f, 1f, 1f, 1f);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            graphics.drawString(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        if (barGuiW > 0) {
            blitScaled(graphics, SCALE_TEXTURE, barX, barY, barGuiW, BAR_GUI_H, TEX_SCALE_W, TEX_SCALE_H);

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    float texFillW = visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW);
                    graphics.pose().pushPose();
                    graphics.pose().translate((float) fillGuiX, (float) fillGuiY, 0f);
                    graphics.pose().scale((float) fillGuiW / TEX_FILL_W, 1f, 1f);
                    graphics.blit(FILL_TEXTURE, 0, 0, 0.0f, 0.0f, Math.round(texFillW), TEX_FILL_H, TEX_FILL_W, TEX_FILL_H);
                    graphics.pose().popPose();
                }
            }
        }

        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private static void blitScaled(GuiGraphics graphics, ResourceLocation texture, int x, int y, int guiW, int guiH, int texW, int texH) {
        graphics.pose().pushPose();
        graphics.pose().translate((float) x, (float) y, 0f);
        graphics.pose().scale((float) guiW / texW, (float) guiH / texH, 1f);
        graphics.blit(texture, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH);
        graphics.pose().popPose();
    }
}