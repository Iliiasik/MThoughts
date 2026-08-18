package mt.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface WellRestedAppliedCallback {

    Event<WellRestedAppliedCallback> EVENT = EventFactory.createArrayBacked(
            WellRestedAppliedCallback.class,
            listeners -> (player, level, mvp, durationTicks) -> {
                for (WellRestedAppliedCallback listener : listeners) {
                    listener.onWellRestedApplied(player, level, mvp, durationTicks);
                }
            });

    void onWellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks);
}
