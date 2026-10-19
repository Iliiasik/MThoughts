package mt.client.ui.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MidnightThoughtsConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 23;
    private static final int FIRST_BUTTON_TOP = 34;
    private static final int DONE_BOTTOM = 32;
    private static final int TITLE_TOP = 14;
    private static final int NOTE_GAP = 8;
    private static final int ICON_GAP = 8;
    private static final int NOTE_COLOR = 0xA0A0A0;

    private final Screen parent;

    public MidnightThoughtsConfigScreen(Screen parent) {
        super(Component.translatable(ConfigScreenAssets.TITLE_KEY).withStyle(ChatFormatting.BOLD));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        if (ConfigScreenAssets.editable()) {
            ConfigSection[] sections = ConfigSection.values();
            for (int i = 0; i < sections.length; i++) {
                ConfigSection section = sections[i];
                addRenderableWidget(Button.builder(section.title(),
                                button -> Minecraft.getInstance().setScreen(new ConfigSectionScreen(this, section)))
                        .bounds(x, FIRST_BUTTON_TOP + i * BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build());
            }
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x, this.height - DONE_BOTTOM, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, TITLE_TOP, 0xFFFFFF);

        int secondNote = this.height - DONE_BOTTOM - NOTE_GAP - this.font.lineHeight;
        int firstNote = secondNote - this.font.lineHeight - 2;
        graphics.drawCenteredString(this.font, Component.translatable(ConfigScreenAssets.NOTE_WELL_RESTED_KEY),
                this.width / 2, firstNote, NOTE_COLOR);
        graphics.drawCenteredString(this.font, Component.translatable(ConfigScreenAssets.NOTE_SERVER_FILE_KEY),
                this.width / 2, secondNote, NOTE_COLOR);

        if (ConfigScreenAssets.editable()) {
            renderIconAboveNotes(graphics, firstNote);
        } else {
            renderIconWithNotice(graphics);
        }
    }

    private void renderIconAboveNotes(GuiGraphics graphics, int firstNote) {
        int buttonsBottom = FIRST_BUTTON_TOP
                + (ConfigSection.values().length - 1) * BUTTON_SPACING + BUTTON_HEIGHT;
        int y = firstNote - ICON_GAP - ConfigScreenAssets.ICON_SIZE;
        if (y < buttonsBottom + ICON_GAP) return;
        ConfigScreenAssets.drawIcon(graphics, this.width / 2 - ConfigScreenAssets.ICON_SIZE / 2, y);
    }

    private void renderIconWithNotice(GuiGraphics graphics) {
        int blockHeight = ConfigScreenAssets.ICON_SIZE + ICON_GAP + this.font.lineHeight;
        int top = this.height / 2 - blockHeight / 2;
        ConfigScreenAssets.drawIcon(graphics, this.width / 2 - ConfigScreenAssets.ICON_SIZE / 2, top);
        graphics.drawCenteredString(this.font, Component.translatable(ConfigScreenAssets.NOTE_KEY),
                this.width / 2, top + ConfigScreenAssets.ICON_SIZE + ICON_GAP, NOTE_COLOR);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
