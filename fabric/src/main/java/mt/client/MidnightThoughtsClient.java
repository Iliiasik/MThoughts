package mt.client;

import mt.MTConstants;
import mt.client.network.ClientNetworkHandler;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitializeClient() {
        MTClient.init();

        registerEventListeners();
        registerResourceReloadListener();
        ClientNetworkHandler.installBridge();
        ClientNetworkHandler.registerPacketHandlers();

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts initialized successfully!");
    }

    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> MTClient.tick(client.player));

        HudElementRegistry.attachElementBefore(VanillaHudElements.HELD_ITEM_TOOLTIP,
                Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "well_rested_hud"), (context, _) -> {
            Minecraft mc = Minecraft.getInstance();
            WellRestedHud.render(context, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.SLEEP,
                Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "sleep_overlay"), (context, _) -> {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            MTClient.overlay().renderOverlayOnly(context, width, height);
            MTClient.overlay().renderContentOnly(context, width, height);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "sleeping_players_hud"), (context, _) -> {
            Minecraft mc = Minecraft.getInstance();
            SleepingPlayersHud.render(context, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> MTClient.onDisconnect());
    }

    @SuppressWarnings("deprecation")
    private void registerResourceReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public @NotNull Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "slide_reloader");
                    }

                    @Override
                    public void onResourceManagerReload(@NotNull ResourceManager manager) {
                        MTClient.reloadSlides(manager);
                    }
                }
        );
    }
}
