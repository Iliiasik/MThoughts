package mt.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class WellRestedExpiredEvent extends Event {
    private final ServerPlayer player;

    public WellRestedExpiredEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}