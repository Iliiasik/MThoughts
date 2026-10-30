package mt.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface WellRestedExpiredCallback {

    Event<WellRestedExpiredCallback> EVENT = EventFactory.createArrayBacked(
            WellRestedExpiredCallback.class,
            listeners -> player -> {
                for (WellRestedExpiredCallback listener : listeners) {
                    listener.onWellRestedExpired(player);
                }
            });

    void onWellRestedExpired(ServerPlayer player);
}
