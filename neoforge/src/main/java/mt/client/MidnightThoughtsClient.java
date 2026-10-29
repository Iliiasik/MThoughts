package mt.client;

import mt.MTConstants;
import mt.client.ui.config.MidnightThoughtsConfigScreen;
import mt.platform.MTClientNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MidnightThoughtsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private MidnightThoughtsClient() {}

    public static void registerModBusListeners(IEventBus modEventBus) {
        modEventBus.addListener(MidnightThoughtsClient::onRegisterReloadListeners);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterGuiLayers);
    }

    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (owner, parent) -> new MidnightThoughtsConfigScreen(parent));
    }

    public static void init() {
        MTClient.init();
        MTClientNetwork.setBridge(PacketDistributor::sendToServer);
        NeoForge.EVENT_BUS.register(new NeoForgeClientEvents());
        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
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

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.SELECTED_ITEM_NAME, layerId("well_rested_hud"),
                (graphics, deltaTracker) -> MTClient.renderWellRestedHud(graphics));
        event.registerAbove(VanillaGuiLayers.SLEEP_OVERLAY, layerId("sleep_overlay"),
                (graphics, deltaTracker) -> MTClient.renderSleepOverlay(graphics));
        event.registerBelow(VanillaGuiLayers.CHAT, layerId("sleeping_players_hud"),
                (graphics, deltaTracker) -> MTClient.renderSleepingPlayersHud(graphics));
    }

    private static ResourceLocation layerId(String name) {
        return ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID, name);
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
