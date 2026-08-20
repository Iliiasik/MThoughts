package mt.client;

import mt.cache.ClientAchievementCache;
import mt.cache.ServerConfigCache;
import mt.client.api.UselessFactsApiClient;
import mt.client.manager.SleepStateManager;
import mt.client.manager.WellRestedClientState;
import mt.client.network.ClientNetworkHandler;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.SlideService;
import mt.client.service.UserContentLoader;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import mt.config.MidnightThoughtsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient implements ClientModInitializer {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static MidnightThoughtsClient instance;

    private final SlideRepository slideRepository;
    private final SleepStateManager sleepStateManager;
    private final SleepOverlayRenderer overlayRenderer;
    private final UserContentLoader userContentLoader;

    public MidnightThoughtsClient() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();
        userContentLoader = new UserContentLoader();

        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository, userContentLoader);
        SlideService slideService = new SlideService(slideRepository, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        ClientNetworkHandler.register();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "slides");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        slideRepository.clearCache();
                        slideRepository.loadAllSlides(manager);
                    }
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            sleepStateManager.tick(client.player);
            overlayRenderer.tick();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ServerConfigCache.clear();
            ClientAchievementCache.clear();
            WellRestedClientState.reset();
            SleepingPlayersHud.reset();
            userContentLoader.clearServerContent();
        });

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts client setup complete");
    }

    public static void renderWellRestedHud(GuiGraphics graphics) {
        WellRestedHud.render(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    public static void renderSleepOverlay(GuiGraphics graphics) {
        MidnightThoughtsClient inst = getInstance();
        if (inst == null) return;

        SleepOverlayRenderer renderer = inst.overlayRenderer;
        if (!renderer.shouldHideCrosshair()) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        renderer.renderOverlayOnly(graphics, width, height);
        graphics.flush();
        renderer.renderContentOnly(graphics, width, height);
        graphics.flush();
    }

    public static void renderSleepingPlayersHud(GuiGraphics graphics) {
        SleepingPlayersHud.render(graphics, graphics.guiWidth(), graphics.guiHeight());
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
}
