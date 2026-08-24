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
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
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
    @SuppressWarnings("deprecation")
    public void onInitializeClient() {
        instance = this;

        ClientNetworkHandler.register();
        registerHudElements();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public @NotNull Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(MOD_ID, "slides");
                    }

                    @Override
                    public void onResourceManagerReload(@NotNull ResourceManager manager) {
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

    private void registerHudElements() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.HELD_ITEM_TOOLTIP,
                Identifier.fromNamespaceAndPath(MOD_ID, "well_rested_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    WellRestedHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                });

        HudElementRegistry.attachElementAfter(VanillaHudElements.SLEEP,
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

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "sleeping_players_hud"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.options.hideGui) return;

                    SleepingPlayersHud.render(graphics,
                            mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
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
}
