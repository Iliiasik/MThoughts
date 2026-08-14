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
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
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
        modEventBus.addListener(MidnightThoughtsClient::onAddReloadListeners);
        modEventBus.addListener(MidnightThoughtsClient::onRegisterGuiLayers);
    }

    public static void init() {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            NeoForge.EVENT_BUS.register(new NeoForgeClientTickEvents());
            LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
        }
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.SELECTED_ITEM_NAME,
                Identifier.fromNamespaceAndPath(MOD_ID, "well_rested_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    WellRestedHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                });

        event.registerAbove(VanillaGuiLayers.SLEEP_OVERLAY,
                Identifier.fromNamespaceAndPath(MOD_ID, "sleep_overlay"),
                (graphics, deltaTracker) -> {
                    MidnightThoughtsClient inst = getInstance();
                    if (inst == null) return;

                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return;

                    int width = mc.getWindow().getGuiScaledWidth();
                    int height = mc.getWindow().getGuiScaledHeight();
                    inst.overlayRenderer.renderOverlayOnly(graphics, width, height);
                    inst.overlayRenderer.renderContentOnly(graphics, width, height);
                });

        event.registerBelow(VanillaGuiLayers.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "sleeping_players_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    SleepingPlayersHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                });
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

    private static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "slide_reloader"), new SimplePreparableReloadListener<ResourceManager>() {
            @Override
            protected @NotNull ResourceManager prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                return manager;
            }

            @Override
            protected void apply(@NotNull ResourceManager manager, @NotNull ResourceManager unused, @NotNull ProfilerFiller profiler) {
                if (instance != null) {
                    instance.slideRepository.clearCache();
                    instance.slideRepository.loadAllSlides(manager);
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

    private static class NeoForgeClientTickEvents {

        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
                if (inst != null) {
                    inst.sleepStateManager.tick(client.player);
                    inst.overlayRenderer.tick();
                }
            }
        }

        @SubscribeEvent
        public void onClientDisconnect(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
            ServerConfigCache.clear();
            ClientAchievementCache.clear();
            mt.client.manager.WellRestedClientState.reset();
            mt.client.ui.SleepingPlayersHud.reset();
            MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
            if (inst != null && inst.userContentLoader != null) {
                inst.userContentLoader.clearServerContent();
            }
        }
    }
}