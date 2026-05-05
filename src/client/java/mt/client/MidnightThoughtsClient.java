package mt.client;

import mt.cache.ClientAchievementCache;
import mt.cache.ServerConfigCache;
import mt.client.api.UselessFactsApiClient;
import mt.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.network.ClientNetworkHandler;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.SlideService;
import mt.client.service.UserContentLoader;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;
    private UserContentLoader userContentLoader;

    @Override
    public void onInitializeClient() {
        instance = this;

        initializeComponents();
        registerEventListeners();
        registerResourceReloadListener();
        ClientNetworkHandler.registerPacketHandlers();

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
    }

    private void initializeComponents() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();
        userContentLoader = new UserContentLoader();

        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository, userContentLoader);
        SlideService slideService = new SlideService(slideRepository, config, factProvider);
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
            overlayRenderer.renderOverlayOnly(context, width, height);
            overlayRenderer.renderContentOnly(context, width, height);
            SleepingPlayersHud.render(context, width, height);
            WellRestedHud.render(context, height);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ServerConfigCache.clear();
            ClientAchievementCache.clear();
            if (userContentLoader != null) {
                userContentLoader.clearServerContent();
            }
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

    public UserContentLoader getUserContentLoader() {
        return userContentLoader;
    }
}