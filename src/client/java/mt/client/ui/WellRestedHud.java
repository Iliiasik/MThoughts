package mt.client.ui;

import mt.client.MidnightThoughtsClient;
import mt.client.manager.WellRestedClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class WellRestedHud {
    private static final Identifier SCALE_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/scale.png");
    private static final Identifier FILL_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/fill.png");
    private static final Identifier ICON_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/well_rested.png");
    private static final Identifier MVP_ICON_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/shared/mvp.png");

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

    public static boolean isActive() {
        return WellRestedClientState.isActive();
    }

    public static void render(DrawContext context, int screenWidth, int screenHeight) {
        if (!WellRestedClientState.isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        TextRenderer font = mc.textRenderer;

        int level = WellRestedClientState.getLevel();
        int ticksRemaining = WellRestedClientState.getTicksRemaining();
        int totalTicks = WellRestedClientState.getTotalTicks();

        float progress = totalTicks > 0 ? (float) ticksRemaining / totalTicks : 0f;

        int barY = screenHeight - MARGIN_BOTTOM - BAR_GUI_H;
        int startX = MARGIN_LEFT;

        boolean isMvp = WellRestedClientState.isMvp();
        Identifier activeIcon = isMvp ? MVP_ICON_TEXTURE : ICON_TEXTURE;

        String roman = (!isMvp && level >= 1 && level <= 5) ? ROMAN[level] : "";
        int romanWidth = roman.isEmpty() ? 0 : font.getWidth(roman);

        int iconX = startX;
        int romanX = iconX + ICON_GUI_SIZE + GAP;
        int barX = romanX + (roman.isEmpty() ? 0 : romanWidth + GAP);
        int barEndX = barX + TEX_SCALE_W;
        int barGuiW = barEndX - barX;

        context.getMatrices().push();
        context.getMatrices().translate(iconX, barY, 0);
        context.getMatrices().scale((float) ICON_GUI_SIZE / TEX_ICON_SIZE, (float) ICON_GUI_SIZE / TEX_ICON_SIZE, 1.0f);
        context.drawTexture(activeIcon, 0, 0, 0.0f, 0.0f, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE, TEX_ICON_SIZE);
        context.getMatrices().pop();

        if (!roman.isEmpty()) {
            int romanY = barY + (BAR_GUI_H - font.fontHeight) / 2;
            context.drawText(font, roman, romanX, romanY, 0xFFFFD966, true);
        }

        if (barGuiW > 0) {
            context.getMatrices().push();
            context.getMatrices().translate(barX, barY, 0);
            context.getMatrices().scale((float) barGuiW / TEX_SCALE_W, (float) BAR_GUI_H / TEX_SCALE_H, 1.0f);
            context.drawTexture(SCALE_TEXTURE, 0, 0, 0.0f, 0.0f, TEX_SCALE_W, TEX_SCALE_H, TEX_SCALE_W, TEX_SCALE_H);
            context.getMatrices().pop();

            if (progress > 0f) {
                int fillGuiW = barGuiW - FILL_INSET * 2;
                int fillGuiX = barX + FILL_INSET;
                int fillGuiY = barY + (BAR_GUI_H - TEX_FILL_H) / 2;
                int visibleFillGuiW = Math.round(fillGuiW * progress);
                if (visibleFillGuiW > 0) {
                    float texFillW = visibleFillGuiW * ((float) TEX_FILL_W / fillGuiW);
                    context.getMatrices().push();
                    context.getMatrices().translate(fillGuiX, fillGuiY, 0);
                    context.getMatrices().scale((float) fillGuiW / TEX_FILL_W, 1f, 1.0f);
                    context.drawTexture(FILL_TEXTURE, 0, 0, 0.0f, 0.0f, Math.round(texFillW), TEX_FILL_H, TEX_FILL_W, TEX_FILL_H);
                    context.getMatrices().pop();
                }
            }
        }
    }
}