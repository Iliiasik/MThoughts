package mt.client;

import mt.client.api.UselessFactsApiClient;
import mt.client.config.MidnightThoughtsConfig;
import mt.client.manager.SleepStateManager;
import mt.client.render.SleepOverlayRenderer;
import mt.client.repository.SlideRepository;
import mt.client.service.FactProvider;
import mt.client.service.SlideService;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MidnightThoughtsClient {
    public static final String MOD_ID = "midnightthoughts";

    private static MidnightThoughtsClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;

    public static void init(IEventBus modEventBus) {
        if (instance == null) {
            instance = new MidnightThoughtsClient();
            MinecraftForge.EVENT_BUS.register(new ForgeClientTickEvents());
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
        SlideService slideService = new SlideService(slideRepository, config, factProvider);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    public void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
            @Override
            protected ResourceManager prepare(ResourceManager manager, ProfilerFiller profiler) {
                return manager;
            }

            @Override
            protected void apply(ResourceManager manager, ResourceManager unused, ProfilerFiller profiler) {
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

    private static class ForgeClientTickEvents {
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
    }
}