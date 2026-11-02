package mt.client;

import mt.MTConstants;
import mt.client.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitializeClient() {
        MTClient.init();
        ClientNetworkHandler.register();

        ResourceManagerReloadListener slideReloader = MTClient::reloadSlides;
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(layerId("slide_reloader"), slideReloader);

        HudElementRegistry.attachElementBefore(VanillaHudElements.HELD_ITEM_TOOLTIP, layerId("well_rested_hud"),
                (graphics, _) -> MTClient.renderWellRestedHud(graphics));
        HudElementRegistry.attachElementAfter(VanillaHudElements.SLEEP, layerId("sleep_overlay"),
                (graphics, _) -> MTClient.renderSleepOverlay(graphics));
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, layerId("sleeping_players_hud"),
                (graphics, _) -> MTClient.renderSleepingPlayersHud(graphics));

        ClientTickEvents.END_CLIENT_TICK.register(client -> MTClient.tick(client.player));
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> MTClient.onDisconnect());

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts client setup complete");
    }

    private static Identifier layerId(String name) {
        return Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, name);
    }
}
