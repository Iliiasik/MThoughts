package mt.client.ui;

import mt.client.network.ClientPacketHandlers;
import mt.client.ui.summary.*;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private final List<DailySummaryPacket.PlayerDailySummary> allPlayers;
    private final List<AchievementTooltipArea> achievementAreas = new ArrayList<>();
    private int currentPage = 0;
    private float fadeAlpha = 0.0f;
    private final long screenOpenTime;
    private final long animationStartTime;
    private final SummaryDimensions dimensions = new SummaryDimensions();
    private ThemeSwitchButton themeSwitchButton;

    public DailySummaryScreen(List<DailySummaryPacket.PlayerDailySummary> players) {
        super(Component.literal("Summary"));
        this.allPlayers = new ArrayList<>(players);
        this.allPlayers.sort((p1, p2) -> {
            if (p1.isMvp() && !p2.isMvp()) return -1;
            if (!p1.isMvp() && p2.isMvp()) return 1;
            return 0;
        });
        this.screenOpenTime = System.currentTimeMillis();
        this.animationStartTime = System.currentTimeMillis() + 300;
    }

    @Override
    protected void init() {
        super.init();

        dimensions.calculate(width, height);

        int buttonWidth = dimensions.getButtonWidth();
        int buttonHeight = dimensions.getButtonHeight();
        int buttonSpacing = dimensions.s(8);
        int totalPages = dimensions.getTotalPages(allPlayers.size());

        int buttonY = dimensions.panelY + dimensions.panelHeight + dimensions.s(8);

        if (totalPages > 1) {
            addNavigationButtons(buttonWidth, buttonHeight, buttonY, buttonSpacing);
        }

        addThemeSwitchButton();
    }

    private void addThemeSwitchButton() {
        int iconSize = dimensions.s(21);
        int iconX = dimensions.panelX + dimensions.panelWidth + dimensions.s(7);
        int iconY = dimensions.panelY + dimensions.s(11);
        themeSwitchButton = new ThemeSwitchButton(iconX, iconY, iconSize);
        addRenderableWidget(themeSwitchButton);
    }

    private void addNavigationButtons(int buttonWidth, int buttonHeight, int buttonY, int buttonSpacing) {
        int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
        int navStartX = width / 2 - navButtonsWidth / 2;

        addRenderableWidget(new StyledButton(
                navStartX, buttonY, buttonWidth, buttonHeight,
                Component.translatable("midnightthoughts.summary.previous"),
                button -> navigateToPreviousPage()
        ));

        addRenderableWidget(new StyledButton(
                navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
                Component.translatable("midnightthoughts.summary.next"),
                button -> navigateToNextPage()
        ));
    }

    private void navigateToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            rebuildWidgets();
        }
    }

    private void navigateToNextPage() {
        int totalPages = dimensions.getTotalPages(allPlayers.size());
        if (currentPage < totalPages - 1) {
            currentPage++;
            rebuildWidgets();
        }
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void onClose() {
        ClientPacketHandlers.sendSummaryAcknowledge();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        updateFadeAnimation();
        achievementAreas.clear();

        dimensions.calculate(width, height);

        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        TooltipRenderer.render(context, font, mouseX, mouseY, achievementAreas, width, fadeAlpha);

        if (themeSwitchButton != null && themeSwitchButton.isMouseOver(mouseX, mouseY)) {
            context.renderTooltip(font, themeSwitchButton.getTooltipText(), mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context) {
    }

    private void updateFadeAnimation() {
        long elapsedTime = System.currentTimeMillis() - screenOpenTime;
        fadeAlpha = Math.min(1.0f, elapsedTime / 300.0f);
    }

    private void renderPanel(GuiGraphics context) {
        FrameRenderer.render(context, dimensions, fadeAlpha);
        FrameRenderer.renderBadge(context, font, dimensions, fadeAlpha);
        int totalPages = dimensions.getTotalPages(allPlayers.size());
        FrameRenderer.renderPagesHolder(context, font, dimensions, currentPage, totalPages, fadeAlpha);
    }

    private void renderPlayerList(GuiGraphics context) {
        int contentPaddingTop = dimensions.s(53);
        int contentPaddingSides = dimensions.s(60);

        int startIndex = currentPage * dimensions.playersPerPage;
        int endIndex = Math.min(startIndex + dimensions.playersPerPage, allPlayers.size());

        int startY = dimensions.panelY + contentPaddingTop;

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * dimensions.playerRowHeight;
            PlayerRowRenderer.render(context, font, player, dimensions.panelX + contentPaddingSides,
                    rowY, dimensions, fadeAlpha, animationStartTime, achievementAreas);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}