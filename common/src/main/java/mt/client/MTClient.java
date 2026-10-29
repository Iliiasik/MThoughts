package mt.client;

import mt.cache.ClientAchievementCache;
import mt.cache.ServerConfigCache;
import mt.client.api.UselessFactsApiClient;
import mt.client.manager.SleepStateManager;
import mt.client.manager.WellRestedClientState;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.SlideService;
import mt.client.service.UserContentLoader;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.config.MidnightThoughtsConfig;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public final class MTClient {

    private static SlideRepository slideRepository;
    private static SleepStateManager sleepStateManager;
    private static SleepOverlayRenderer overlayRenderer;
    private static UserContentLoader userContentLoader;
    private static List<DailySummaryPacket.PlayerDailySummary> pendingSummary;

    private MTClient() {}

    public static void init() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();
        userContentLoader = new UserContentLoader();

        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository, userContentLoader);
        SlideService slideService = new SlideService(slideRepository, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    public static SleepOverlayRenderer overlay() {
        return overlayRenderer;
    }

    public static UserContentLoader userContent() {
        return userContentLoader;
    }

    public static void showSummary(List<DailySummaryPacket.PlayerDailySummary> summaries) {
        pendingSummary = summaries;
    }

    public static void tick(LocalPlayer player) {
        if (player == null || sleepStateManager == null) return;
        sleepStateManager.tick(player);
        overlayRenderer.tick();
        if (pendingSummary != null && !player.isSleeping()) {
            Minecraft.getInstance().setScreen(new DailySummaryScreen(pendingSummary));
            pendingSummary = null;
        }
    }

    public static void reloadSlides(ResourceManager manager) {
        if (slideRepository == null) return;
        slideRepository.clearCache();
        slideRepository.loadAllSlides(manager);
    }

    public static void onDisconnect() {
        pendingSummary = null;
        ServerConfigCache.clear();
        ClientAchievementCache.clear();
        WellRestedClientState.reset();
        SleepingPlayersHud.reset();
        if (userContentLoader != null) {
            userContentLoader.clearServerContent();
        }
    }
}
