package mt.client;

import mt.client.api.UselessFactsApiClient;
import mt.client.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.PlayerStatsService;
import mt.client.service.SlideService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient implements ClientModInitializer {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static MidnightThoughtsClient instance;

    private MidnightThoughtsConfig config;
    private SlideRepository slideRepository;
    private PlayerStatsService playerStatsService;
    private UselessFactsApiClient apiClient;
    private FactProvider factProvider;
    private SlideService slideService;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing Midnight Thoughts...");

        initializeComponents();
        registerEventListeners();
        registerResourceReloadListener();

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void initializeComponents() {
        config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();
        playerStatsService = new PlayerStatsService();
        apiClient = new UselessFactsApiClient();
        factProvider = new FactProvider(apiClient, slideRepository);
        slideService = new SlideService(slideRepository, playerStatsService, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                sleepStateManager.tick(client.player);
                overlayRenderer.tick();
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();
            overlayRenderer.render(context, width, height);
        });
    }

    private void registerResourceReloadListener() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.of(MOD_ID, "slide_reloader");
                }

                @Override
                public void reload(ResourceManager manager) {
                    LOGGER.info("Reloading slide data...");
                    slideRepository.clearCache();
                    slideRepository.loadAllSlides(manager);
                }
            }
        );
    }

    public static MidnightThoughtsClient getInstance() {
        return instance;
    }

    public SleepOverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }
}

