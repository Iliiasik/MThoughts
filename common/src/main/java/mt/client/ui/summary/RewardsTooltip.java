package mt.client.ui.summary;

import mt.network.packet.DailySummaryPacket;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class RewardsTooltip {

    private static final String TITLE_KEY = "midnightthoughts.summary.rewards";
    private static final String EXPERIENCE_KEY = "midnightthoughts.summary.rewards.experience";
    private static final String WELL_RESTED_KEY = "midnightthoughts.summary.rewards.well_rested";
    private static final String WELL_RESTED_MVP_KEY = "midnightthoughts.summary.rewards.well_rested_mvp";

    private RewardsTooltip() {}

    public static List<String> lines(DailySummaryPacket.PlayerDailySummary player) {
        List<String> rewards = new ArrayList<>(2);
        if (player.experience() > 0) {
            rewards.add(Component.translatable(EXPERIENCE_KEY, player.experience()).getString());
        }
        if (player.wellRestedLevel() > 0) {
            rewards.add(player.isMvp()
                    ? Component.translatable(WELL_RESTED_MVP_KEY).getString()
                    : Component.translatable(WELL_RESTED_KEY, player.wellRestedLevel()).getString());
        }
        if (rewards.isEmpty()) return List.of();

        List<String> lines = new ArrayList<>(rewards.size() + 1);
        lines.add(Component.translatable(TITLE_KEY).getString());
        lines.addAll(rewards);
        return lines;
    }
}
