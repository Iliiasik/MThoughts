package mt.client;

import mt.MTConstants;
import mt.client.network.ClientPacketHandlers;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import mt.platform.MTClientNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = MTConstants.MOD_ID, dist = Dist.CLIENT)
public class MidnightThoughtsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughtsClient(IEventBus modEventBus) {
        MTClient.init();

        MTClientNetwork.setBridge(ClientPacketDistributor::sendToServer);

        modEventBus.addListener(this::onAddReloadListeners);
        modEventBus.addListener(this::onRegisterClientPayloadHandlers);
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts client setup complete");
    }

    private void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(DailySummaryPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleDailySummary(packet)));
        event.register(SleepingPlayersPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSleepingPlayers(packet)));
        event.register(WellRestedPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleWellRested(packet)));
        event.register(UserContentPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleUserContent(packet)));
        event.register(SyncAchievementsPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSyncAchievements(packet)));
        event.register(SyncConfigPacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSyncConfig(packet)));
        event.register(MoonPhasePacket.TYPE,
                (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleMoonPhase(packet)));
    }

    private void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "slide_reloader"),
                new SimplePreparableReloadListener<ResourceManager>() {
                    @Override
                    protected @NotNull ResourceManager prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                        return manager;
                    }

                    @Override
                    protected void apply(@NotNull ResourceManager manager, @NotNull ResourceManager unused, @NotNull ProfilerFiller profiler) {
                        MTClient.reloadSlides(manager);
                    }
                });
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        MTClient.tick(Minecraft.getInstance().player);
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        MTClient.onDisconnect();
    }
}
