package mt;

import mt.command.MidnightThoughtsCommand;
import mt.network.NetworkHandler;
import mt.platform.NeoForgeBridges;
import mt.server.MTServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MTConstants.MOD_ID)
public class MidnightThoughts {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughts(IEventBus modEventBus, ModContainer container) {
        NeoForgeBridges.install();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(NetworkHandler::registerPackets);
        if (!FMLEnvironment.production) {
            modEventBus.addListener(RegisterGameTestsEvent.class, mt.gametest.NeoForgeGameTestHooks::register);
        }
        if (FMLEnvironment.dist.isClient()) {
            mt.client.MidnightThoughtsClient.registerModBusListeners(modEventBus);
            mt.client.MidnightThoughtsClient.registerConfigScreen(container);
        }
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        MTServer.init();
        LOGGER.info("Midnight Thoughts common setup complete");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init();
        LOGGER.info("Midnight Thoughts client setup complete");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MTServer.tick(event.getServer());
    }

    @SubscribeEvent
    public void onCanPlayerSleep(CanPlayerSleepEvent event) {
        var problem = MTServer.trySleep(event.getEntity(), event.getPos());
        if (problem != null) event.setProblem(problem);
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.wakeImmediately() || !event.updateLevel()) return;
        if (event.getEntity() instanceof ServerPlayer player) MTServer.onWokeUp(player);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MTServer.onDeath(player);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Midnight Thoughts server started");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MidnightThoughtsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MTServer.onJoin(player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MTServer.onLeave(player);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        MTServer.onStopping(event.getServer());
        LOGGER.info("Midnight Thoughts server stopping");
    }
}
