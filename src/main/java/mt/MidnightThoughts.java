package mt;

import mt.network.NetworkHandler;
import mt.network.packet.WellRestedPacket;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.SleepTracker;
import mt.server.WellRestedEffect;
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
        NetworkHandler.registerPackets();
        AchievementLoader.load();
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
            boolean active = WellRestedEffect.hasEffect(player);
            int level = WellRestedEffect.getLevel(player);
            int ticksRemaining = WellRestedEffect.getTicksRemaining(player);
            int totalTicks = active ? WellRestedEffect.getTotalDurationTicksForPlayer(player) : 0;
            int phase = WellRestedEffect.getCurrentPhase(player);
            boolean nightmare = ComfortCalculator.isNightmareMode(player);
            boolean mvp = WellRestedEffect.isMvp(player);
            NetworkHandler.sendWellRested(player, new WellRestedPacket(active, level, ticksRemaining, totalTicks, phase, nightmare, mvp));
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;

        MinecraftServer srv = serverPlayer.server;
        if (srv != null) {
            SleepTracker tracker = DailyStatsManager.getSleepTracker(srv);
            if (tracker != null) tracker.markPlayerSlept(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ComfortCalculator.isSleepBlocked(player)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            player.displayClientMessage(
                    Component.translatable("midnightthoughts.sleep.nightmare_blocked"),
                    true
            );
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WellRestedEffect.removeFromPlayer(player);
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DailyStatsManager.initialize();
        LOGGER.info("Midnight Thoughts server started");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerJoin(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerLeave(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DailyStatsManager.onServerStop(event.getServer());
        LOGGER.info("Midnight Thoughts server stopping");
    }
}