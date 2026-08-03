package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public class WellRestedExpiredKubeEvent extends EventJS {
    private final ServerPlayer player;

    public WellRestedExpiredKubeEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() { return player; }
}