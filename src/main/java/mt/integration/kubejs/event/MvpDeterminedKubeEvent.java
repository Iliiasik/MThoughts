package mt.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.server.level.ServerPlayer;

public class MvpDeterminedKubeEvent extends EventJS {
    private final ServerPlayer mvp;

    public MvpDeterminedKubeEvent(ServerPlayer mvp) {
        this.mvp = mvp;
    }

    public ServerPlayer getMvp() { return mvp; }
}