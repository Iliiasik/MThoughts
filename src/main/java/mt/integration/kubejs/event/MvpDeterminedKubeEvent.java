package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerPlayer;

public class MvpDeterminedKubeEvent implements KubeEvent {
    private final ServerPlayer mvp;

    public MvpDeterminedKubeEvent(ServerPlayer mvp) {
        this.mvp = mvp;
    }

    public ServerPlayer getMvp() { return mvp; }
}
