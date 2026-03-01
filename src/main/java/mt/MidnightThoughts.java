package mt;

import mt.network.NetworkHandler;
import mt.server.DailyStatsManager;
import mt.server.WellRestedEffect;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightThoughts.MOD_ID)
public class MidnightThoughts {
    public static final String MOD_ID = "midnightthoughts";
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MOD_ID);
    public static final DeferredHolder<MobEffect, WellRestedEffect> WELL_RESTED = EFFECTS.register("well_rested", WellRestedEffect::new);
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private final IEventBus modEventBus;

    public MidnightThoughts(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
        EFFECTS.register(modEventBus);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterPayloads);
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Thoughts initialized successfully!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Midnight Thoughts common setup complete");
    }

    private void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        NetworkHandler.registerPackets(event);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init(modEventBus);
        LOGGER.info("Midnight Thoughts client setup complete");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        DailyStatsManager.tick(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DailyStatsManager.initialize();
        LOGGER.info("Midnight Thoughts server started");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerJoin(serverPlayer);
            LOGGER.debug("Player joined: {}", serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerLeave(serverPlayer);
            LOGGER.debug("Player left: {}", serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DailyStatsManager.onServerStop(event.getServer());
        LOGGER.info("Midnight Thoughts server stopping");
    }
}