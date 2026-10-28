package mt.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

@SuppressWarnings("unused")
public class WellRestedAppliedEvent extends Event {
    private final ServerPlayer player;
    private final int level;
    private final boolean mvp;
    private final int durationTicks;

    public WellRestedAppliedEvent(ServerPlayer player, int level, boolean mvp, int durationTicks) {
        this.player = player;
        this.level = level;
        this.mvp = mvp;
        this.durationTicks = durationTicks;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getLevel() { return level; }
    public boolean isMvp() { return mvp; }
    public int getDurationTicks() { return durationTicks; }
}
