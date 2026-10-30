package mt;

import mt.command.MidnightThoughtsCommand;
import mt.network.NetworkHandler;
import mt.platform.ForgeBridges;
import mt.server.MTServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MTConstants.MOD_ID)
public class MidnightThoughts {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughts(FMLJavaModLoadingContext context) {
        ForgeBridges.install();

        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        if (!FMLEnvironment.production) {
            modEventBus.addListener(mt.gametest.ForgeGameTestHooks::register);
        }
        if (FMLEnvironment.dist.isClient()) {
            mt.client.MidnightThoughtsClient.registerModBusListeners(modEventBus);
            mt.client.MidnightThoughtsClient.registerConfigScreen(context);
        }
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerPackets();
        MTServer.init();
        LOGGER.info("Midnight Thoughts common setup complete");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init();
        LOGGER.info("Midnight Thoughts client setup complete");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) MTServer.tick(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var problem = MTServer.trySleep(player, event.getPos());
        if (problem != null) event.setResult(problem);
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
