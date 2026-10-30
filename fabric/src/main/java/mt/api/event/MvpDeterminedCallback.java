package mt.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MvpDeterminedCallback {

    Event<MvpDeterminedCallback> EVENT = EventFactory.createArrayBacked(
            MvpDeterminedCallback.class,
            listeners -> mvp -> {
                for (MvpDeterminedCallback listener : listeners) {
                    listener.onMvpDetermined(mvp);
                }
            });

    void onMvpDetermined(ServerPlayer mvp);
}
