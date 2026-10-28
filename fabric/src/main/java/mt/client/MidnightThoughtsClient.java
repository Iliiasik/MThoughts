package mt.client;

import mt.MTConstants;
import mt.client.network.ClientNetworkHandler;
import mt.client.render.SleepOverlayRenderer;
import mt.client.ui.SleepingPlayersHud;
import mt.client.ui.WellRestedHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    public static void renderWellRestedHud(GuiGraphics graphics) {
        if (Minecraft.getInstance().player == null) return;
        WellRestedHud.render(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    public static void renderSleepOverlay(GuiGraphics graphics) {
        SleepOverlayRenderer overlay = MTClient.overlay();
        if (overlay == null || Minecraft.getInstance().player == null) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        overlay.renderOverlayOnly(graphics, width, height);
        overlay.renderContentOnly(graphics, width, height);
    }

    public static void renderSleepingPlayersHud(GuiGraphics graphics) {
        if (Minecraft.getInstance().player == null) return;
        SleepingPlayersHud.render(graphics, graphics.guiWidth(), graphics.guiHeight());
    }
}
