package mt.client.ui.config;

import mt.MTConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class ConfigScreenAssets {
    static final Identifier ICON =
            Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "textures/gui/shared/icon.png");

    static final String TITLE_KEY = "screen.midnightthoughts.config";
    static final String NOTE_WELL_RESTED_KEY = "screen.midnightthoughts.config.note.well_rested";
    static final String NOTE_SERVER_FILE_KEY = "screen.midnightthoughts.config.note.server_file";
    static final String NOTE_SERVER_OTHER_KEY = "screen.midnightthoughts.config.note.server_other";

    static final int ICON_SIZE = 40;

    private static final int ICON_TEXTURE_SIZE = 40;

    private ConfigScreenAssets() {}

    static boolean editable() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null || mc.hasSingleplayerServer();
    }

    static void drawIcon(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, x, y, 0f, 0f, ICON_SIZE, ICON_SIZE,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}
