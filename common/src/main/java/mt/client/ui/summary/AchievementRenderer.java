package mt.client.ui.summary;

import mt.cache.ClientAchievementCache;
import mt.client.ui.engine.RenderHelper;
import mt.client.ui.engine.RenderUtils;
import mt.server.AchievementDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class AchievementRenderer {

    private AchievementRenderer() {}

    public static void render(GuiGraphics context, List<String> achievements,
                              SummaryLayout layout, float fadeAlpha, List<HoverArea> areas) {
        int count = Math.min(achievements.size(), SummaryLayout.ACHIEVEMENT_COUNT);
        if (count == 0) return;

        int size = layout.achievementSize();
        int step = size + layout.s(SummaryLayout.ACHIEVEMENT_GAP);
        int startX = RenderUtils.centeredX(layout.panelX(), layout.panelWidth(),
                layout.achievementRowWidth(count));
        int y = layout.achievementsY();

        for (int i = 0; i < count; i++) {
            int x = startX + i * step;
            String id = achievements.get(i);

            RenderHelper.blitTexture(context, SummaryConstants.getAchievementTexture(),
                    x, y, size, size, fadeAlpha);

            areas.add(new HoverArea(x, y, size, size, describe(id)));
        }
    }

    private static List<String> describe(String achievementId) {
        for (AchievementDefinition def : ClientAchievementCache.get()) {
            if (def.id.equals(achievementId)) {
                List<String> lines = new ArrayList<>(2);
                lines.add(def.name);
                if (def.tooltip != null && !def.tooltip.isBlank()) lines.add(def.tooltip);
                return lines;
            }
        }

        List<String> lines = new ArrayList<>(2);
        lines.add(Component.translatable("midnightthoughts.achievement." + achievementId).getString());
        String key = "midnightthoughts.achievement." + achievementId + ".desc";
        String description = Component.translatable(key).getString();
        if (!description.equals(key)) lines.add(description);
        return lines;
    }
}
