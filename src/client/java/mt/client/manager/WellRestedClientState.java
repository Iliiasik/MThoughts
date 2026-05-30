package mt.client.manager;

public class WellRestedClientState {
    private static boolean active = false;
    private static int level = 0;
    private static int ticksRemaining = 0;
    private static int totalTicks = 0;
    private static boolean nightmareMode = false;
    private static boolean mvp = false;

    public static void update(boolean active, int level, int ticksRemaining, int totalTicks, boolean nightmareMode, boolean mvp) {
        WellRestedClientState.active = active;
        WellRestedClientState.level = level;
        WellRestedClientState.ticksRemaining = ticksRemaining;
        WellRestedClientState.totalTicks = totalTicks;
        WellRestedClientState.nightmareMode = nightmareMode;
        WellRestedClientState.mvp = mvp;
    }

    public static boolean isActive() { return active; }
    public static int getLevel() { return level; }
    public static int getTicksRemaining() { return ticksRemaining; }
    public static int getTotalTicks() { return totalTicks; }
    public static boolean isNightmareMode() { return nightmareMode; }
    public static boolean isMvp() { return mvp; }
}