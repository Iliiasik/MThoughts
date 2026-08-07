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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class MidnightThoughtsClient {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static MidnightThoughtsClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;
    private UserContentLoader userContentLoader;

    public static void init(IEventBus modEventBus) {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            NeoForge.EVENT_BUS.register(new NeoForgeClientEvents());
            modEventBus.addListener(instance::onRegisterReloadListeners);
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

    public void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
            @Nonnull
            @Override
            protected ResourceManager prepare(@Nonnull ResourceManager manager, @Nonnull ProfilerFiller profiler) {
                return manager;
            }

            @Override
            protected void apply(@Nonnull ResourceManager manager, @Nonnull ResourceManager unused, @Nonnull ProfilerFiller profiler) {
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

    private static class NeoForgeClientEvents {

        private record RenderContext(MidnightThoughtsClient inst, Minecraft mc, int w, int h) {}

        private static RenderContext getRenderContext() {
            MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
            if (inst == null) return null;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return null;
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            return new RenderContext(inst, mc, w, h);
        }

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
        public void onRenderGuiPre(RenderGuiEvent.Pre event) {
            RenderContext ctx = getRenderContext();
            if (ctx == null) return;
            ctx.inst().overlayRenderer.renderOverlayOnly(event.getGuiGraphics(), ctx.w(), ctx.h());
            if (WellRestedHud.isPrimaryPosition()) {
                WellRestedHud.render(event.getGuiGraphics(), ctx.w(), ctx.h());
            }
        }

        @SubscribeEvent
        public void onRenderGuiPost(RenderGuiEvent.Post event) {
            RenderContext ctx = getRenderContext();
            if (ctx == null) return;
            ctx.inst().overlayRenderer.renderContentOnly(event.getGuiGraphics(), ctx.w(), ctx.h());
            SleepingPlayersHud.render(event.getGuiGraphics(), ctx.w(), ctx.h());
            if (!WellRestedHud.isPrimaryPosition()) {
                WellRestedHud.render(event.getGuiGraphics(), ctx.w(), ctx.h());
            }
        }

        @SubscribeEvent
        public void onClientDisconnect(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
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
