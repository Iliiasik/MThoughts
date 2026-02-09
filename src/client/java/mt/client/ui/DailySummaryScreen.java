package mt.client.ui;

import mt.client.network.ClientNetworkHandler;
import mt.client.ui.summary.*;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private final List<DailySummaryPacket.PlayerDailySummary> allPlayers;
    private final List<AchievementTooltipArea> achievementAreas = new ArrayList<>();
    private final SummaryDimensions dims = new SummaryDimensions();
    private int currentPage = 0;
    private float fadeAlpha = 0.0f;
    private final long screenOpenTime;
    private final long animationStartTime;

    public DailySummaryScreen(List<DailySummaryPacket.PlayerDailySummary> players) {
        super(Text.literal("Summary"));
        this.allPlayers = new ArrayList<>(players);
        this.allPlayers.sort(Comparator.comparing(DailySummaryPacket.PlayerDailySummary::isMvp).reversed());
        this.screenOpenTime = System.currentTimeMillis();
        this.animationStartTime = System.currentTimeMillis() + 300;
    }

    @Override
    protected void init() {
        super.init();
        dims.calculate(width, height);

        int buttonWidth = dims.getButtonWidth();
        int buttonHeight = dims.getButtonHeight();
        int buttonY = dims.panelY + dims.panelHeight + (int)(6 * dims.uiScale);
        int buttonSpacing = (int)(6 * dims.uiScale);

        int totalPages = dims.getTotalPages(allPlayers.size());

        if (totalPages > 1) {
            int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
            int navStartX = width / 2 - navButtonsWidth / 2;

            addDrawableChild(new StyledButton(
                navStartX, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.previous"),
                button -> {
                    if (currentPage > 0) {
                        currentPage--;
                        clearAndInit();
                    }
                }
            ));

            addDrawableChild(new StyledButton(
                navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.next"),
                button -> {
                    if (currentPage < totalPages - 1) {
                        currentPage++;
                        clearAndInit();
                    }
                }
            ));

            buttonY += buttonHeight + buttonSpacing;
        }

        addDrawableChild(new StyledButton(
            width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight,
            Text.translatable("midnightthoughts.summary.continue"),
            button -> {
                ClientNetworkHandler.sendSummaryAcknowledge();
                close();
            }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long elapsedTime = System.currentTimeMillis() - screenOpenTime;
        fadeAlpha = Math.min(1.0f, elapsedTime / 300.0f);

        achievementAreas.clear();

        BackgroundRenderer.render(context, width, height);
        FrameRenderer.render(context, dims, fadeAlpha);
        FrameRenderer.renderBadge(context, textRenderer, dims, fadeAlpha);

        int totalPages = dims.getTotalPages(allPlayers.size());
        FrameRenderer.renderPagesHolder(context, textRenderer, dims, currentPage, totalPages, fadeAlpha);

        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        TooltipRenderer.render(context, textRenderer, mouseX, mouseY, achievementAreas, width, height, fadeAlpha);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void renderPlayerList(DrawContext context) {
        float contentPaddingTop = SummaryConstants.FRAME_CONTENT_PADDING_TOP * (dims.panelHeight / 640.0f);
        float contentPaddingSides = SummaryConstants.FRAME_CONTENT_PADDING_SIDES * (dims.panelWidth / 1000.0f);

        int startY = dims.panelY + (int)contentPaddingTop;
        int startX = dims.panelX + (int)contentPaddingSides;
        int startIndex = currentPage * dims.playersPerPage;
        int endIndex = Math.min(startIndex + dims.playersPerPage, allPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * dims.playerRowHeight;
            PlayerRowRenderer.render(context, textRenderer, player, startX, rowY, dims, fadeAlpha, animationStartTime, achievementAreas);
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

