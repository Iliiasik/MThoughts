package mt.gametest;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ApiEventLog {

    public record Entry(String event, UUID player, int value, boolean flag, int extra) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static int comfortBonus;

    private ApiEventLog() {}

    public static void record(String event, ServerPlayer player, int value, boolean flag, int extra) {
        if (!TestHarness.isInstalled()) return;
        ENTRIES.add(new Entry(event, player.getUUID(), value, flag, extra));
    }

    public static int comfortBonus() {
        return comfortBonus;
    }

    public static void setComfortBonus(int bonus) {
        comfortBonus = bonus;
    }

    public static List<Entry> of(String event, UUID player) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (entry.event().equals(event) && entry.player().equals(player)) result.add(entry);
        }
        return result;
    }

    public static void reset() {
        ENTRIES.clear();
        comfortBonus = 0;
    }
}
