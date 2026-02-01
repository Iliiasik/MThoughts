package mt.client;

import mt.client.api.UselessFactsApiClient;
import mt.client.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.PlayerStatsService;
import mt.client.service.SlideService;
import mt.client.ui.SleepingPlayersHud;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MidnightThoughtsClient {
    public static final String MOD_ID = "midnightthoughts";

    private static MidnightThoughtsClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;

    public static void init() {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            MinecraftForge.EVENT_BUS.register(new ForgeClientEvents());
        }
    }

    private MidnightThoughtsClient() {
        initializeComponents();
    }

    private void initializeComponents() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        slideRepository = new SlideRepository();

        Minecraft mc = Minecraft.getInstance();
        if (mc.getResourceManager() != null) {
            slideRepository.loadAllSlides(mc.getResourceManager());
        }

        PlayerStatsService playerStatsService = new PlayerStatsService();
        UselessFactsApiClient apiClient = new UselessFactsApiClient();
        FactProvider factProvider = new FactProvider(apiClient, slideRepository);
        SlideService slideService = new SlideService(slideRepository, playerStatsService, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    public static MidnightThoughtsClient getInstance() {
        return instance;
    }

    public SleepOverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }

    private static class ForgeClientEvents {

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
                    inst.sleepStateManager.tick(client.player);
                    inst.overlayRenderer.tick();
                }
            }
        }

        @SubscribeEvent
        public void onRenderGuiPre(RenderGuiOverlayEvent.Pre event) {
            if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

            Minecraft mc = Minecraft.getInstance();
            MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
            if (mc.player == null || inst == null) return;

            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();

            inst.overlayRenderer.renderOverlayOnly(event.getGuiGraphics(), w, h);
        }

        @SubscribeEvent
        public void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
            if (mc.player == null || inst == null) return;

            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();

            inst.overlayRenderer.renderContentOnly(event.getGuiGraphics(), w, h);
            SleepingPlayersHud.render(event.getGuiGraphics(), w, h);
        }

        @SubscribeEvent
        public void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
                @Override
                protected ResourceManager prepare(ResourceManager manager, ProfilerFiller profiler) {
                    return manager;
                }

                @Override
                protected void apply(ResourceManager manager, ResourceManager unused, ProfilerFiller profiler) {
                    MidnightThoughtsClient inst = MidnightThoughtsClient.getInstance();
                    if (inst != null) {
                        inst.slideRepository.clearCache();
                        inst.slideRepository.loadAllSlides(manager);
                    }
                }
            });
        }
    }
}
