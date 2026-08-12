package mt;

import mt.command.MidnightThoughtsCommand;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.platform.MTEvents;
import mt.platform.MTNetwork;
import mt.platform.NeoForgeBridges;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.UserContentInitializer;
import mt.server.WellRestedEffect;
import mt.server.WellRestedSync;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
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

@Mod(MTConstants.MOD_ID)
public class MidnightThoughts {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughts(IEventBus modEventBus) {
        NeoForgeBridges.install();

        modEventBus.addListener(this::onRegisterPayloads);
        NeoForge.EVENT_BUS.register(this);

        AchievementLoader.load();
        UserContentInitializer.writeDefaultFiles();

        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        NetworkHandler.registerPackets(event);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MidnightThoughtsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DailyStatsManager.initialize();
        LOGGER.info("Midnight Thoughts server started");
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
    @SuppressWarnings("resource")
    public void onCanPlayerSleep(CanPlayerSleepEvent event) {
        ServerPlayer serverPlayer = event.getEntity();
        if (ComfortCalculator.isSleepBlocked(serverPlayer)) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            serverPlayer.sendSystemMessage(
                    Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                    true
            );
        } else {
            SleepTracker tracker = DailyStatsManager.getSleepTracker(serverPlayer.level().getServer());
            if (tracker != null) tracker.markPlayerSleeping(serverPlayer.getUUID());
            if (ComfortCalculator.isNightmareMode(serverPlayer)) {
                MTEvents.nightmare(serverPlayer, ComfortCalculator.calculateComfortLevel(serverPlayer));
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;
        SleepTracker tracker = DailyStatsManager.getSleepTracker(serverPlayer.level().getServer());
        if (tracker != null) tracker.markPlayerWoke(serverPlayer.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WellRestedEffect.clear(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        DailyStatsManager.onPlayerJoin(serverPlayer);
        MTNetwork.sendToPlayer(serverPlayer, UserContentInitializer.buildPacket());
        MTNetwork.sendToPlayer(serverPlayer, new SyncAchievementsPacket(AchievementLoader.load()));
        MTNetwork.sendToPlayer(serverPlayer, SyncConfigPacket.of(MidnightThoughtsConfig.getInstance()));
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
        UserContentInitializer.invalidateCache();
    }
}
