package mt.client.ui;

import mt.client.MidnightThoughtsClient;
import mt.client.manager.WellRestedClientState;
import mt.server.config.MidnightThoughtsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class WellRestedHud {
    private static final ResourceLocation SCALE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/scale.png");
    private static final ResourceLocation FILL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/fill.png");
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/well_rested.png");
    private static final ResourceLocation MVP_ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/mvp.png");

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

    private static final int MARGIN_LEFT = 4;
    private static final int MARGIN_BOTTOM = 4;

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};
    private static final int BLINK_THRESHOLD_TICKS = 200;

    public static boolean isActive() {
        return WellRestedClientState.isActive();
    }

    public static boolean isPrimaryPosition() {
        return "primary".equals(MidnightThoughtsConfig.getInstance().getWellRestedHudPosition());
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (!WellRestedClientState.isActive() || MidnightThoughtsConfig.getInstance().isHideWellRestedHud()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;

        int level = WellRestedClientState.getLevel();
        int ticksRemaining = WellRestedClientState.getTicksRemaining();
        int totalTicks = WellRestedClientState.getTotalTicks();

        float progress = totalTicks > 0 ? (float) ticksRemaining / totalTicks : 0f;

        boolean isMvp = WellRestedClientState.isMvp();
        ResourceLocation activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.width(roman);

        float scaleAlpha = 1.0f;
        if (ticksRemaining <= BLINK_THRESHOLD_TICKS && ticksRemaining > 0) {
            float sin = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 3.0f);
            scaleAlpha = 0.4f + 0.6f * (sin * 0.5f + 0.5f);
        }

        int iconX;
        int barY;

        if (isPrimaryPosition()) {
            int totalRight = screenWidth / 2 + 91;
            int totalLeft = totalRight - TOTAL_GUI_W;
            barY = screenHeight - 32 - BAR_GUI_H - 10;
            iconX = totalLeft;
        } else {
            barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;
            iconX = MARGIN_LEFT;
        }

        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + (roman.isEmpty() ? 0 : romanWidth + GAP);

        int barGuiW;
        if (isPrimaryPosition()) {
            int totalRight = screenWidth / 2 + 91;
            barGuiW = totalRight - barX;
        } else {
            barGuiW = TEX_SCALE_W;
        }

        graphics.setColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate((float) iconX, (float) barY, 0f);
        graphics.pose().scale((float) ICON_GUI_SIZE / TEX_ICON_SIZE, (float) ICON_GUI_SIZE / TEX_ICON_SIZE, 1f);
        graphics.blit(activeIcon, 0, 0, 0.0f, 0.0f, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE);
        graphics.pose().popPose();
        graphics.setColor(1f, 1f, 1f, scaleAlpha);

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.lineHeight) / 2;
            graphics.drawString(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        if (barGuiW > 0) {
            graphics.setColor(1f, 1f, 1f, scaleAlpha);
            graphics.pose().pushPose();
            graphics.pose().translate((float) barX, (float) barY, 0f);
            graphics.pose().scale((float) barGuiW / TEX_SCALE_W, (float) BAR_GUI_H / TEX_SCALE_H, 1f);
            graphics.blit(SCALE_TEXTURE, 0, 0, 0.0f, 0.0f, TEX_SCALE_W, TEX_SCALE_H, TEX_SCALE_W, TEX_SCALE_H);
            graphics.pose().popPose();

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    float texFillW = visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW);
                    graphics.setColor(1f, 1f, 1f, 1f);
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
}