package mt.client;

import mt.client.api.UselessFactsApiClient;
import mt.server.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.PlayerStatsService;
import mt.client.service.SlideService;
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

import javax.annotation.Nonnull;

public class MidnightThoughtsClient {
    public static final String MOD_ID = "midnightthoughts";

    private static MidnightThoughtsClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;

    public static void init(IEventBus modEventBus) {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            NeoForge.EVENT_BUS.register(new NeoForgeClientTickEvents());
            modEventBus.addListener(instance::onRegisterReloadListeners);
        }
    }

    private MidnightThoughtsClient() {
        initializeComponents();
    }

    private void initializeComponents() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();

        Minecraft mc = Minecraft.getInstance();
        slideRepository.loadAllSlides(mc.getResourceManager());

        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository);
        PlayerStatsService playerStatsService = new PlayerStatsService();
        SlideService slideService = new SlideService(slideRepository, playerStatsService, config, factProvider);
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
        public void onRenderGuiPre(RenderGuiEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            WellRestedHud.render(event.getGuiGraphics(),
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight());
        }
    }
}