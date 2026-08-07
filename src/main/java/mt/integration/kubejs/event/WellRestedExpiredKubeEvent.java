package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class WellRestedExpiredKubeEvent implements KubeEvent {
    private final ServerPlayer player;

    public WellRestedExpiredKubeEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}
