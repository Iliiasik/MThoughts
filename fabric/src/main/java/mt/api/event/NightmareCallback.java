package mt.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface NightmareCallback {

    Event<NightmareCallback> EVENT = EventFactory.createArrayBacked(
            NightmareCallback.class,
            listeners -> (player, comfortLevel) -> {
                for (NightmareCallback listener : listeners) {
                    listener.onNightmare(player, comfortLevel);
                }
            });

    void onNightmare(ServerPlayer player, int comfortLevel);
}
