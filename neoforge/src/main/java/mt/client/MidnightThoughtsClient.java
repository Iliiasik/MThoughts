package mt.client;

import mt.MTConstants;
import mt.client.network.ClientPacketHandlers;
import mt.client.ui.config.MidnightThoughtsConfigScreen;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.PlayerSleepStatePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.platform.MTClientNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MidnightThoughtsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private MidnightThoughtsClient() {}

    public static void registerModBusListeners(IEventBus modEventBus) {
        modEventBus.addListener(MidnightThoughtsClient::onAddReloadListeners);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterGuiLayers);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterPayloadHandlers);
    }

    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (_, parent) -> new MidnightThoughtsConfigScreen(parent));
    }

    public static void init() {
        MTClient.init();
        MTClientNetwork.setBridge(ClientPacketDistributor::sendToServer);
        NeoForge.EVENT_BUS.register(new NeoForgeClientEvents());
        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
    }

    private static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(layerId("slide_reloader"), new SimplePreparableReloadListener<ResourceManager>() {
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

    private static void onRegisterPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(DailySummaryPacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleDailySummary(packet)));
        event.register(SleepingPlayersPacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSleepingPlayers(packet)));
        event.register(PlayerSleepStatePacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSleepState(packet)));
        event.register(UserContentPacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleUserContent(packet)));
        event.register(SyncAchievementsPacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSyncAchievements(packet)));
        event.register(SyncConfigPacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleSyncConfig(packet)));
        event.register(MoonPhasePacket.TYPE, (packet, ctx) -> ctx.enqueueWork(() -> ClientPacketHandlers.handleMoonPhase(packet)));
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.SELECTED_ITEM_NAME, layerId("well_rested_hud"),
                (graphics, _) -> MTClient.renderWellRestedHud(graphics));
        event.registerAbove(VanillaGuiLayers.SLEEP_OVERLAY, layerId("sleep_overlay"),
                (graphics, _) -> MTClient.renderSleepOverlay(graphics));
        event.registerBelow(VanillaGuiLayers.CHAT, layerId("sleeping_players_hud"),
                (graphics, _) -> MTClient.renderSleepingPlayersHud(graphics));
    }

    private static Identifier layerId(String name) {
        return Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, name);
    }

    private static class NeoForgeClientEvents {

        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            MTClient.tick(Minecraft.getInstance().player);
        }

        @SubscribeEvent
        public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            MTClient.onDisconnect();
        }
    }
}
