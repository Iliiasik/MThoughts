package mt;

import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.UserContentInitializer;
import mt.server.WellRestedEffect;
import mt.server.WellRestedSync;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import mt.command.MidnightThoughtsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightThoughts.MOD_ID)
public class MidnightThoughts {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private final IEventBus modEventBus;

    public MidnightThoughts() {
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        MidnightThoughtsConfig.getInstance();
        NetworkHandler.registerPackets();
        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();
        LOGGER.info("Midnight Thoughts common setup complete");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init(modEventBus);
        LOGGER.info("Midnight Thoughts client setup complete");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        DailyStatsManager.tick(event.getServer());
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            WellRestedEffect.tick(player);
            WellRestedSync.sync(player);
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer srv = serverPlayer.server;
        SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
        if (tracker != null) {
            tracker.markPlayerSleeping(serverPlayer.getUUID());
        }
        if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            serverPlayer.displayClientMessage(
                    Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                    true
            );
        } else if (ComfortCalculator.isNightmareMode(serverPlayer)) {
            MinecraftForge.EVENT_BUS.post(new mt.api.event.NightmareEvent(
                    serverPlayer, ComfortCalculator.calculateComfortLevel(serverPlayer)));
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;
        MinecraftServer srv = serverPlayer.server;
        SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
        if (tracker != null) tracker.markPlayerWoke(serverPlayer.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean had = WellRestedEffect.hasEffect(player);
            WellRestedEffect.removeFromPlayer(player);
            if (had) {
                MinecraftForge.EVENT_BUS.post(new mt.api.event.WellRestedExpiredEvent(player));
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DailyStatsManager.initialize();
        LOGGER.info("Midnight Thoughts server started");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        DailyStatsManager.onPlayerJoin(serverPlayer);
        NetworkHandler.sendUserContent(serverPlayer, UserContentInitializer.buildPacket());
        NetworkHandler.sendAchievements(serverPlayer, new SyncAchievementsPacket(AchievementLoader.load()));
        MidnightThoughtsConfig cfg = MidnightThoughtsConfig.getInstance();
        NetworkHandler.sendConfig(serverPlayer, new SyncConfigPacket(
                cfg.getSleepOverlay().minSlideDisplayTimeMs,
                cfg.getSleepOverlay().maxSlideDisplayTimeMs,
                cfg.getSleepOverlay().fadeInDurationMs,
                cfg.getSleepOverlay().fadeOutDurationMs,
                cfg.getSleepOverlay().overlayOpacity,
                cfg.getSleepOverlay().textOpacity,
                cfg.getSleepOverlay().imageOpacity,
                cfg.getSleepOverlay().specialSlideChance,
                cfg.getSleepOverlay().enableOverlay,
                cfg.getSleepOverlay().enableImage,
                cfg.getSleepOverlay().enableDailySummaryScreen,
                cfg.getSleepOverlay().useFactsApi,
                cfg.getSleepOverlay().userContentReplaces,
                cfg.getSleepOverlay().hideChatWhenSleeping,
                cfg.getUi().theme,
                cfg.getUi().hideWellRestedHud,
                cfg.getUi().hideThemeSwitchButton,
                cfg.getSleepOverlay().enableStarDust,
                cfg.getSleepOverlay().showSlideProgress
        ));
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ComfortCalculator.invalidateCache(serverPlayer);
            DailyStatsManager.onPlayerLeave(serverPlayer);
            WellRestedSync.clear(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DailyStatsManager.onServerStop(event.getServer());
        LOGGER.info("Midnight Thoughts server stopping");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MidnightThoughtsCommand.register(event.getDispatcher());
    }
}