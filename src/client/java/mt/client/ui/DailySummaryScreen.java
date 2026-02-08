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
        int buttonSpacing = (int)(6 * dimensions.uiScale);
        int totalPages = dimensions.getTotalPages(allPlayers.size());

        int buttonY = dimensions.panelY + dimensions.panelHeight + (int)(6 * dimensions.uiScale);

        if (totalPages > 1) {
            buttonY = addNavigationButtons(buttonWidth, buttonHeight, buttonY, buttonSpacing);
        }

        addContinueButton(buttonWidth, buttonHeight, buttonY);
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

    private void closeScreen() {
        ClientNetworkHandler.sendSummaryAcknowledge();
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateFadeAnimation();
        achievementAreas.clear();

        dimensions.calculate(width, height);

        BackgroundRenderer.render(context, width, height);
        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        TooltipRenderer.render(context, textRenderer, mouseX, mouseY, achievementAreas, width, height, fadeAlpha);
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
        float contentPaddingTop = SummaryConstants.FRAME_CONTENT_PADDING_TOP * (dimensions.panelHeight / 640.0f);
        float contentPaddingSides = SummaryConstants.FRAME_CONTENT_PADDING_SIDES * (dimensions.panelWidth / 1000.0f);

        int startY = dimensions.panelY + (int)contentPaddingTop;
        int startIndex = currentPage * dimensions.playersPerPage;
        int endIndex = Math.min(startIndex + dimensions.playersPerPage, allPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * dimensions.playerRowHeight;
            PlayerRowRenderer.render(context, textRenderer, player, dimensions.panelX + (int)contentPaddingSides,
                                    rowY, dimensions, fadeAlpha, animationStartTime, achievementAreas);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
    }
}