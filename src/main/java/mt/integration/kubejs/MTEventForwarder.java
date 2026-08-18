package mt.integration.kubejs;

import mt.api.event.ComfortCalculatedCallback;
import mt.api.event.MvpDeterminedCallback;
import mt.api.event.NightmareCallback;
import mt.api.event.WellRestedAppliedCallback;
import mt.api.event.WellRestedExpiredCallback;
import mt.integration.kubejs.event.ComfortCalculatedKubeEvent;
import mt.integration.kubejs.event.MvpDeterminedKubeEvent;
import mt.integration.kubejs.event.NightmareKubeEvent;
import mt.integration.kubejs.event.WellRestedAppliedKubeEvent;
import mt.integration.kubejs.event.WellRestedExpiredKubeEvent;

public final class MTEventForwarder {

    private MTEventForwarder() {}

    public static void register() {
        WellRestedAppliedCallback.EVENT.register((player, level, mvp, durationTicks) ->
                MTKubeEvents.WELL_RESTED_APPLIED.post(
                        new WellRestedAppliedKubeEvent(player, level, mvp, durationTicks)));

        WellRestedExpiredCallback.EVENT.register(player ->
                MTKubeEvents.WELL_RESTED_EXPIRED.post(new WellRestedExpiredKubeEvent(player)));

        NightmareCallback.EVENT.register((player, comfortLevel) ->
                MTKubeEvents.NIGHTMARE.post(new NightmareKubeEvent(player, comfortLevel)));

        MvpDeterminedCallback.EVENT.register(mvp ->
                MTKubeEvents.MVP_DETERMINED.post(new MvpDeterminedKubeEvent(mvp)));

        ComfortCalculatedCallback.EVENT.register((player, level) -> {
            ComfortCalculatedKubeEvent event = new ComfortCalculatedKubeEvent(player, level);
            MTKubeEvents.COMFORT_CALCULATED.post(event);
            return event.getLevel();
        });
    }
}
