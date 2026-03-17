package mt.client.ui;

import mt.client.network.ClientNetworkHandler;
import mt.client.ui.summary.*;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
        super(Text.literal("Summary"));
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
            buttonY = addNavigationButtons(buttonWidth, buttonHeight, buttonY, buttonSpacing);
        }

        addContinueButton(buttonWidth, buttonHeight, buttonY);
        addThemeSwitchButton();
    }

    private void addThemeSwitchButton() {
        int iconSize = dimensions.s(21);
        int iconX = dimensions.panelX + dimensions.panelWidth + dimensions.s(7);
        int iconY = dimensions.panelY + dimensions.s(11);
        themeSwitchButton = new ThemeSwitchButton(iconX, iconY, iconSize);
        addDrawableChild(themeSwitchButton);
    }

    private int addNavigationButtons(int buttonWidth, int buttonHeight, int buttonY, int buttonSpacing) {
        int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
        int navStartX = width / 2 - navButtonsWidth / 2;

        addDrawableChild(new StyledButton(
                navStartX, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.previous"),
                button -> navigateToPreviousPage()
        ));

        addDrawableChild(new StyledButton(
                navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.next"),
                button -> navigateToNextPage()
        ));

        return buttonY + buttonHeight + buttonSpacing;
    }

    private void addContinueButton(int buttonWidth, int buttonHeight, int buttonY) {
        addDrawableChild(new StyledButton(
                width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.continue"),
                button -> closeScreen()
        ));
    }

    private void navigateToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            clearAndInit();
        }
    }

    private void navigateToNextPage() {
        int totalPages = dimensions.getTotalPages(allPlayers.size());
        if (currentPage < totalPages - 1) {
            currentPage++;
            clearAndInit();
        }
    }

    @Override
    protected void clearAndInit() {
        this.clearChildren();
        this.init();
    }

    private void closeScreen() {
        ClientNetworkHandler.sendSummaryAcknowledge();
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        updateFadeAnimation();
        achievementAreas.clear();

        dimensions.calculate(width, height);

        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        TooltipRenderer.render(context, textRenderer, mouseX, mouseY, achievementAreas, width, height, fadeAlpha);

        if (themeSwitchButton != null && themeSwitchButton.isMouseOver(mouseX, mouseY)) {
            context.drawTooltip(textRenderer, themeSwitchButton.getTooltipText(), mouseX, mouseY);
        }
    }

    private void updateFadeAnimation() {
        long elapsedTime = System.currentTimeMillis() - screenOpenTime;
        fadeAlpha = Math.min(1.0f, elapsedTime / 300.0f);
    }

    private void renderPanel(DrawContext context) {
        FrameRenderer.render(context, dimensions, fadeAlpha);
        FrameRenderer.renderBadge(context, textRenderer, dimensions, fadeAlpha);
        int totalPages = dimensions.getTotalPages(allPlayers.size());
        FrameRenderer.renderPagesHolder(context, textRenderer, dimensions, currentPage, totalPages, fadeAlpha);
    }

    private void renderPlayerList(DrawContext context) {
        int contentPaddingTop = dimensions.s(53);
        int contentPaddingSides = dimensions.s(60);

        int startIndex = currentPage * dimensions.playersPerPage;
        int endIndex = Math.min(startIndex + dimensions.playersPerPage, allPlayers.size());

        int startY = dimensions.panelY + contentPaddingTop;

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * dimensions.playerRowHeight;
            PlayerRowRenderer.render(context, textRenderer, player, dimensions.panelX + contentPaddingSides,
                    rowY, dimensions, fadeAlpha, animationStartTime, achievementAreas);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}