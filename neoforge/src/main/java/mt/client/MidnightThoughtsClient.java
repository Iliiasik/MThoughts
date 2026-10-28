package mt.client;

import mt.MTConstants;
import mt.client.render.SleepOverlayRenderer;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
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

    private static boolean initialized;

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
        if (initialized) return;
        initialized = true;

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
        event.registerBelow(VanillaGuiLayers.SELECTED_ITEM_NAME,
                ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID, "well_rested_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    WellRestedHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                });

        event.registerAbove(VanillaGuiLayers.SLEEP_OVERLAY,
                ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID, "sleep_overlay"),
                (graphics, deltaTracker) -> {
                    SleepOverlayRenderer overlay = MTClient.overlay();
                    if (overlay == null) return;

                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return;

                    int width = mc.getWindow().getGuiScaledWidth();
                    int height = mc.getWindow().getGuiScaledHeight();
                    overlay.renderOverlayOnly(graphics, width, height);
                    overlay.renderContentOnly(graphics, width, height);
                });

        event.registerBelow(VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID, "sleeping_players_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    SleepingPlayersHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                });
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
