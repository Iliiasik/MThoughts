package mt.integration.kubejs;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import mt.integration.kubejs.event.ComfortCalculatedKubeEvent;
import mt.integration.kubejs.event.MvpDeterminedKubeEvent;
import mt.integration.kubejs.event.NightmareKubeEvent;
import mt.integration.kubejs.event.WellRestedAppliedKubeEvent;
import mt.integration.kubejs.event.WellRestedExpiredKubeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class MTEventForwarder {

    @SubscribeEvent
    public void onApplied(WellRestedAppliedEvent e) {
        MTKubeEvents.WELL_RESTED_APPLIED.post(new WellRestedAppliedKubeEvent(e.getPlayer(), e.getLevel(), e.isMvp(), e.getDurationTicks()));
    }

    @SubscribeEvent
    public void onExpired(WellRestedExpiredEvent e) {
        MTKubeEvents.WELL_RESTED_EXPIRED.post(new WellRestedExpiredKubeEvent(e.getPlayer()));
    }

    @SubscribeEvent
    public void onNightmare(NightmareEvent e) {
        MTKubeEvents.NIGHTMARE.post(new NightmareKubeEvent(e.getPlayer(), e.getComfortLevel()));
    }

    @SubscribeEvent
    public void onMvp(MvpDeterminedEvent e) {
        MTKubeEvents.MVP_DETERMINED.post(new MvpDeterminedKubeEvent(e.getMvp()));
    }

    @SubscribeEvent
    public void onComfort(ComfortCalculatedEvent e) {
        MTKubeEvents.COMFORT_CALCULATED.post(new ComfortCalculatedKubeEvent(e));
    }
}