package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

public class WellRestedAppliedKubeEvent implements KubeEvent {
    private final ServerPlayer player;
    private final int level;
    private final boolean mvp;
    private final int durationTicks;

    public WellRestedAppliedKubeEvent(ServerPlayer player, int level, boolean mvp, int durationTicks) {
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
