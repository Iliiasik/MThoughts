package mt.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import mt.integration.kubejs.event.ComfortCalculatedKubeEvent;
import mt.integration.kubejs.event.MvpDeterminedKubeEvent;
import mt.integration.kubejs.event.NightmareKubeEvent;
import mt.integration.kubejs.event.WellRestedAppliedKubeEvent;
import mt.integration.kubejs.event.WellRestedExpiredKubeEvent;

public interface MTKubeEvents {
    EventGroup GROUP = EventGroup.of("MidnightThoughtsEvents");

    EventHandler WELL_RESTED_APPLIED = GROUP.server("wellRestedApplied", () -> WellRestedAppliedKubeEvent.class);
    EventHandler WELL_RESTED_EXPIRED = GROUP.server("wellRestedExpired", () -> WellRestedExpiredKubeEvent.class);
    EventHandler NIGHTMARE = GROUP.server("nightmare", () -> NightmareKubeEvent.class);
    EventHandler MVP_DETERMINED = GROUP.server("mvpDetermined", () -> MvpDeterminedKubeEvent.class);
    EventHandler COMFORT_CALCULATED = GROUP.server("comfortCalculated", () -> ComfortCalculatedKubeEvent.class);
}