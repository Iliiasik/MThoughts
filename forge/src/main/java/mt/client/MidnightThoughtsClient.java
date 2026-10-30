package mt.client;

import mt.client.ui.config.MidnightThoughtsConfigScreen;
import mt.network.NetworkHandler;
import mt.platform.MTClientNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MidnightThoughtsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private MidnightThoughtsClient() {}

    public static void registerModBusListeners(IEventBus modEventBus) {
        modEventBus.addListener(MidnightThoughtsClient::onRegisterReloadListeners);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterGuiOverlays);
    }

    public static void registerConfigScreen(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new MidnightThoughtsConfigScreen(parent)));
    }

    public static void init() {
        MTClient.init();
        MTClientNetwork.setBridge(NetworkHandler::sendToServer);
        MinecraftForge.EVENT_BUS.register(new ForgeClientEvents());
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

    private static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.ITEM_NAME.id(), "well_rested_hud",
                (gui, graphics, partialTick, width, height) -> MTClient.renderWellRestedHud(graphics));
        event.registerAbove(VanillaGuiOverlay.SLEEP_FADE.id(), "sleep_overlay",
                (gui, graphics, partialTick, width, height) -> MTClient.renderSleepOverlay(graphics));
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "sleeping_players_hud",
                (gui, graphics, partialTick, width, height) -> MTClient.renderSleepingPlayersHud(graphics));
    }

    private static class ForgeClientEvents {

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) MTClient.tick(Minecraft.getInstance().player);
        }

        @SubscribeEvent
        public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            MTClient.onDisconnect();
        }
    }
}
