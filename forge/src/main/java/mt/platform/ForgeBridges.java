package mt.platform;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import mt.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public final class ForgeBridges {

    private ForgeBridges() {}

    public static void install() {
        MTEvents.setBridge(new MTEvents.Bridge() {
            @Override
            public int comfortCalculated(ServerPlayer player, int level) {
                ComfortCalculatedEvent event = new ComfortCalculatedEvent(player, level);
                MinecraftForge.EVENT_BUS.post(event);
                return event.getLevel();
            }

            @Override
            public void nightmare(ServerPlayer player, int comfortLevel) {
                MinecraftForge.EVENT_BUS.post(new NightmareEvent(player, comfortLevel));
            }

            @Override
            public void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks) {
                MinecraftForge.EVENT_BUS.post(new WellRestedAppliedEvent(player, level, mvp, durationTicks));
            }

            @Override
            public void wellRestedExpired(ServerPlayer player) {
                MinecraftForge.EVENT_BUS.post(new WellRestedExpiredEvent(player));
            }

            @Override
            public void mvpDetermined(ServerPlayer mvp) {
                MinecraftForge.EVENT_BUS.post(new MvpDeterminedEvent(mvp));
            }
        });

        MTNetwork.setBridge(NetworkHandler::sendToPlayer);

        MTPlatform.setBridge(new MTPlatform.Bridge() {
            @Override
            public boolean isModLoaded(String modId) {
                return ModList.get().isLoaded(modId);
            }

            @Override
            public CompoundTag persistentData(Player player) {
                return player.getPersistentData();
            }
        });
    }
}
