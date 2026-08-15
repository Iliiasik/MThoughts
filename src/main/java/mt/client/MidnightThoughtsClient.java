package mt.client;

import mt.cache.ClientAchievementCache;
import mt.cache.ServerConfigCache;
import mt.client.api.UselessFactsApiClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.SlideService;
import mt.client.service.UserContentLoader;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static MidnightThoughtsClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;
    private UserContentLoader userContentLoader;

    public static void registerModBusListeners(IEventBus modEventBus) {
        modEventBus.addListener(MidnightThoughtsClient::onRegisterReloadListeners);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterGuiOverlays);
    }

    private static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.ITEM_NAME.id(), "well_rested_hud",
                (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                    if (gui.getMinecraft().options.hideGui) return;
                    WellRestedHud.render(graphics, screenWidth, screenHeight);
                });

        event.registerAbove(VanillaGuiOverlay.SLEEP_FADE.id(), "sleep_overlay",
                (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                    MidnightThoughtsClient inst = getInstance();
                    if (inst == null) return;

                    SleepOverlayRenderer renderer = inst.overlayRenderer;
                    if (!renderer.shouldHideCrosshair()) return;

                    renderer.renderOverlayOnly(graphics, screenWidth, screenHeight);
                    graphics.flush();
                    renderer.renderContentOnly(graphics, screenWidth, screenHeight);
                    graphics.flush();
                });

        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "sleeping_players_hud",
                (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                    if (gui.getMinecraft().options.hideGui) return;
                    SleepingPlayersHud.render(graphics, screenWidth, screenHeight);
                });
    }

    public static void init() {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            MinecraftForge.EVENT_BUS.register(new ForgeClientEvents());
            LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
        }
    }

    private MidnightThoughtsClient() {
        initializeComponents();
    }

    private void initializeComponents() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();
        userContentLoader = new UserContentLoader();

        Minecraft mc = Minecraft.getInstance();
        slideRepository.loadAllSlides(mc.getResourceManager());

        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository, userContentLoader);
        SlideService slideService = new SlideService(slideRepository, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
            @Override
            protected @NotNull ResourceManager prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                return manager;
            }

            @Override
            protected void apply(@NotNull ResourceManager manager, @NotNull ResourceManager unused, @NotNull ProfilerFiller profiler) {
                MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
                if (inst != null) {
                    inst.slideRepository.clearCache();
                    inst.slideRepository.loadAllSlides(manager);
                }
            }
        });
    }

    public static MidnightThoughtsClient getInstance() {
        return instance;
    }

    public SleepOverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }

    public UserContentLoader getUserContentLoader() {
        return userContentLoader;
    }

    private static class ForgeClientEvents {

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
                    if (inst != null) {
                        inst.sleepStateManager.tick(client.player);
                        inst.overlayRenderer.tick();
                    }
                }
            }
        }

        @SubscribeEvent
        public void onClientDisconnect(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
            ServerConfigCache.clear();
            ClientAchievementCache.clear();
            mt.client.manager.WellRestedClientState.reset();
            SleepingPlayersHud.reset();
            MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
            if (inst != null && inst.userContentLoader != null) {
                inst.userContentLoader.clearServerContent();
            }
        }
    }
}