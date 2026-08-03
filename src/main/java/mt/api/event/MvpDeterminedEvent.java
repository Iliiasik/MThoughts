package mt.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

@SuppressWarnings("unused")
public class MvpDeterminedEvent extends Event {
    private final ServerPlayer mvp;

    public MvpDeterminedEvent(ServerPlayer mvp) {
        this.mvp = mvp;
    }

    public ServerPlayer getMvp() { return mvp; }
}