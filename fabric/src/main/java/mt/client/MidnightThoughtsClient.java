package mt.client;

import mt.MTConstants;
import mt.client.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightThoughtsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    @Override
    public void onInitializeClient() {
        MTClient.init();
        ClientNetworkHandler.register();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(MTConstants.MOD_ID, "slide_reloader");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        MTClient.reloadSlides(manager);
                    }
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> MTClient.tick(client.player));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MTClient.onDisconnect());

        LOGGER.info("[MidnightThoughtsClient] Midnight Thoughts client setup complete");
    }
}
