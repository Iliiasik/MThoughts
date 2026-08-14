package mt;

import mt.command.MidnightThoughtsCommand;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightThoughts.MOD_ID)
public class MidnightThoughts {
    public static final String MOD_ID = "midnightthoughts";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughts(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterPayloads);
        if (FMLEnvironment.getDist().isClient()) {
            mt.client.MidnightThoughtsClient.registerModBusListeners(modEventBus);
        }
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        MidnightThoughtsConfig.getInstance();
        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();
        LOGGER.info("Midnight Thoughts common setup complete");
    }

    private void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        NetworkHandler.registerPackets(event);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init();
        LOGGER.info("Midnight Thoughts client setup complete");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        DailyStatsManager.tick(event.getServer());
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            WellRestedEffect.tick(player);
            WellRestedSync.sync(player);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MidnightThoughtsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;
        MinecraftServer srv = serverPlayer.level().getServer();
        SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
        if (tracker != null) tracker.markPlayerWoke(serverPlayer.getUUID());
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public void onCanPlayerSleep(CanPlayerSleepEvent event) {
        ServerPlayer serverPlayer = event.getEntity();
        if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            serverPlayer.displayClientMessage(
                    Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                    true
            );
        } else {
            MinecraftServer srv = serverPlayer.level().getServer();
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerSleeping(serverPlayer.getUUID());
            if (ComfortCalculator.isNightmareMode(serverPlayer)) {
                NeoForge.EVENT_BUS.post(new mt.api.event.NightmareEvent(
                        serverPlayer, ComfortCalculator.calculateComfortLevel(serverPlayer)));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WellRestedEffect.clear(player);
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
        NetworkHandler.sendConfig(serverPlayer, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
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
}