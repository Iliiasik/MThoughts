package mt;

import mt.network.NetworkHandler;
import mt.server.DailyStatsManager;
import mt.server.WellRestedEffect;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.effect.MobEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightThoughts.MOD_ID)
public class MidnightThoughts {
    public static final String MOD_ID = "midnightthoughts";
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final RegistryObject<WellRestedEffect> WELL_RESTED = EFFECTS.register("well_rested", WellRestedEffect::new);
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public MidnightThoughts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EFFECTS.register(modEventBus);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerPackets();
    }
    private void onClientSetup(final FMLClientSetupEvent event) {
        mt.client.MidnightThoughtsClient.init();
    }
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer() != null) {
            DailyStatsManager.tick(event.getServer());
        }
    }
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        DailyStatsManager.initialize();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerJoin(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DailyStatsManager.onPlayerLeave(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        DailyStatsManager.onServerStop(event.getServer());
    }
}
