package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.network.ClientNetworkHandler;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/background.png");
    private static final Identifier CROWN_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/crown.png");
    private static final Identifier FRAME_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/frame.png");
    private static final Identifier BADGE_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/badge.png");
    private static final Identifier PAGES_HOLDER_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/pages_holder.png");

    private static final long STAT_ANIMATION_DURATION = 800;
    private static final int MVP_GOLD_COLOR = 0xFFD700;
    private static final int FRAME_TEXTURE_WIDTH = 1000;
    private static final int FRAME_TEXTURE_HEIGHT = 640;
    private static final float FRAME_ASPECT_RATIO = 1000.0f / 640.0f;
    private static final float BACKGROUND_ASPECT_RATIO = 16.0f / 9.0f;

    private static final int BADGE_TEXTURE_WIDTH = 480;
    private static final int BADGE_TEXTURE_HEIGHT = 160;

    private static final int PAGES_HOLDER_TEXTURE_WIDTH = 380;
    private static final int PAGES_HOLDER_TEXTURE_HEIGHT = 170;

    private static final int COMPACT_MODE_HEIGHT_THRESHOLD = 350;
    private static final int COMPACT_MODE_WIDTH_THRESHOLD = 500;
    private static final int COMPACT_MODE_PLAYERS_PER_PAGE = 3;
    private static final int NORMAL_MODE_PLAYERS_PER_PAGE = 4;

    private static final int FRAME_CONTENT_PADDING_TOP = 70;
    private static final int FRAME_CONTENT_PADDING_BOTTOM = 50;
    private static final int FRAME_CONTENT_PADDING_SIDES = 80;

    private final List<DailySummaryPacket.PlayerDailySummary> allPlayers;
    private final List<AchievementTooltipArea> achievementAreas = new ArrayList<>();
    private int currentPage = 0;
    private float fadeAlpha = 0.0f;
    private final long screenOpenTime;
    private final long animationStartTime;

    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;
    private int playerRowHeight;
    private int headSize;
    private float uiScale;
    private boolean isCompactMode;
    private int playersPerPage;

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

    private void calculateDimensions() {
        isCompactMode = isCompactModeRequired();

        if (isCompactMode) {
            applyCompactModeDimensions();
        } else {
            applyNormalModeDimensions();
        }

        calculatePanelPosition();
    }

    private boolean isCompactModeRequired() {
        return height < COMPACT_MODE_HEIGHT_THRESHOLD || width < COMPACT_MODE_WIDTH_THRESHOLD;
    }

    private void applyCompactModeDimensions() {
        uiScale = calculateUIScale(420.0f, 300.0f, 0.35f, 0.8f);
        playersPerPage = COMPACT_MODE_PLAYERS_PER_PAGE;

        int maxPanelHeight = Math.min(height - 100, (int)(240 * uiScale));
        panelWidth = (int)(maxPanelHeight * FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > width - 40) {
            panelWidth = width - 40;
            panelHeight = (int)(panelWidth / FRAME_ASPECT_RATIO);
        }

        playerRowHeight = (int)(42 * uiScale);
        headSize = (int)(16 * uiScale);
    }

    private void applyNormalModeDimensions() {
        uiScale = calculateUIScale(854.0f, 480.0f, 0.6f, 1.5f);
        playersPerPage = NORMAL_MODE_PLAYERS_PER_PAGE;

        int maxPanelHeight = Math.min(height - 120, (int)(400 * uiScale));
        panelWidth = (int)(maxPanelHeight * FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > width - 60) {
            panelWidth = width - 60;
            panelHeight = (int)(panelWidth / FRAME_ASPECT_RATIO);
        }

        playerRowHeight = (int)(65 * uiScale);
        headSize = (int)(32 * uiScale);
    }

    private float calculateUIScale(float baseWidth, float baseHeight, float minScale, float maxScale) {
        float scale = Math.min((float)width / baseWidth, (float)height / baseHeight);
        return Math.max(minScale, Math.min(maxScale, scale));
    }

    private void calculatePanelPosition() {
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2 - (int)(30 * uiScale);
    }

    @Override
    protected void init() {
        super.init();
        calculateDimensions();

        int buttonWidth = calculateButtonWidth();
        int buttonHeight = calculateButtonHeight();
        int buttonSpacing = (int)(6 * uiScale);
        int totalPages = calculateTotalPages();

        int buttonY = panelY + panelHeight + (int)(6 * uiScale);

        if (totalPages > 1) {
            buttonY = addNavigationButtons(buttonWidth, buttonHeight, buttonY, buttonSpacing);
        }

        addContinueButton(buttonWidth, buttonHeight, buttonY);
    }

    private int calculateButtonWidth() {
        return isCompactMode ? Math.max(60, (int)(70 * uiScale)) : (int)(90 * uiScale);
    }

    private int calculateButtonHeight() {
        return isCompactMode ? Math.max(16, (int)(16 * uiScale)) : (int)(20 * uiScale);
    }

    private int calculateTotalPages() {
        return Math.max(1, (int) Math.ceil((double) allPlayers.size() / playersPerPage));
    }

    private int addNavigationButtons(int buttonWidth, int buttonHeight, int buttonY, int buttonSpacing) {
        int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
        int navStartX = width / 2 - navButtonsWidth / 2;

        addDrawableChild(new StyledButtonWidget(
            navStartX, buttonY, buttonWidth, buttonHeight,
            Text.translatable("midnightthoughts.summary.previous"),
            button -> navigateToPreviousPage()
        ));

        addDrawableChild(new StyledButtonWidget(
            navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
            Text.translatable("midnightthoughts.summary.next"),
            button -> navigateToNextPage()
        ));

        return buttonY + buttonHeight + buttonSpacing;
    }

    private void addContinueButton(int buttonWidth, int buttonHeight, int buttonY) {
        addDrawableChild(new StyledButtonWidget(
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
        int totalPages = calculateTotalPages();
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

        calculateDimensions();
        renderBackgroundImage(context);
        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        renderAchievementTooltips(context, mouseX, mouseY);
    }

    private void updateFadeAnimation() {
        long elapsedTime = System.currentTimeMillis() - screenOpenTime;
        fadeAlpha = Math.min(1.0f, elapsedTime / 300.0f);
    }

    private void renderBackgroundImage(DrawContext context) {
        setupRenderSystem(BACKGROUND_TEXTURE, 1.0f);

        BackgroundDimensions bgDims = calculateBackgroundDimensions();
        context.drawTexture(BACKGROUND_TEXTURE, bgDims.x, bgDims.y, 0, 0, bgDims.width, bgDims.height, bgDims.width, bgDims.height);
    }

    private BackgroundDimensions calculateBackgroundDimensions() {
        float screenAspect = (float)width / (float)height;

        if (screenAspect > BACKGROUND_ASPECT_RATIO) {
            int bgWidth = width;
            int bgHeight = (int)(width / BACKGROUND_ASPECT_RATIO);
            int bgX = 0;
            int bgY = (height - bgHeight) / 2;
            return new BackgroundDimensions(bgX, bgY, bgWidth, bgHeight);
        } else {
            int bgHeight = height;
            int bgWidth = (int)(height * BACKGROUND_ASPECT_RATIO);
            int bgX = (width - bgWidth) / 2;
            int bgY = 0;
            return new BackgroundDimensions(bgX, bgY, bgWidth, bgHeight);
        }
    }

    private void setupRenderSystem(Identifier texture, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    }

    private record BackgroundDimensions(int x, int y, int width, int height) {}

    private void renderPanel(DrawContext context) {
        renderFrameTexture(context);
        renderPanelTitle(context);
        renderPageInfo(context);
    }

    private void renderFrameTexture(DrawContext context) {
        setupRenderSystem(FRAME_TEXTURE, fadeAlpha);
        RenderSystem.enableBlend();

        context.drawTexture(FRAME_TEXTURE, panelX, panelY, panelWidth, panelHeight,
            0.0f, 0.0f, FRAME_TEXTURE_WIDTH, FRAME_TEXTURE_HEIGHT, FRAME_TEXTURE_WIDTH, FRAME_TEXTURE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private void renderPanelTitle(DrawContext context) {
        Text title = Text.translatable("midnightthoughts.summary.title");

        float badgeScale = panelWidth / 1000.0f;
        int badgeWidth = (int)(240 * badgeScale);
        int badgeHeight = (int)(80 * badgeScale);

        int contentPaddingSides = (int)(FRAME_CONTENT_PADDING_SIDES * (panelWidth / 1000.0f));
        int badgeOffsetX = (int)(15 * badgeScale);
        int badgeOffsetY = (int)(25 * badgeScale);
        int badgeX = panelX + contentPaddingSides + badgeOffsetX;
        int badgeY = panelY - badgeHeight / 2 + badgeOffsetY;

        setupRenderSystem(BADGE_TEXTURE, fadeAlpha);
        RenderSystem.enableBlend();

        context.drawTexture(BADGE_TEXTURE, badgeX, badgeY, badgeWidth, badgeHeight,
            0.0f, 0.0f, BADGE_TEXTURE_WIDTH, BADGE_TEXTURE_HEIGHT, BADGE_TEXTURE_WIDTH, BADGE_TEXTURE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        int titleAlpha = (int)(fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | 0x68503c;

        float textScale = badgeScale * 1.8f;
        int scaledTextWidth = (int)(textRenderer.getWidth(title) * textScale);
        int titleX = badgeX + (badgeWidth - scaledTextWidth) / 2;
        int titleY = badgeY + (badgeHeight - (int)(textRenderer.fontHeight * textScale)) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(titleX, titleY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, title, 0, 0, titleColor, false);
        context.getMatrices().pop();
    }

    private void renderPanelSeparatorLine(DrawContext context) {
        int lineY = panelY + (int)(28 * uiScale);
        int lineColor = (int)(fadeAlpha * 120) << 24 | 0x8a7aaa;
        int linePadding = (int)(15 * uiScale);
        context.fill(panelX + linePadding, lineY, panelX + panelWidth - linePadding, lineY + 1, lineColor);
    }

    private void renderPageInfo(DrawContext context) {
        int totalPages = calculateTotalPages();
        if (totalPages <= 1) {
            return;
        }

        Text pageInfo = Text.translatable("midnightthoughts.summary.page", currentPage + 1, totalPages);

        float pagesHolderScale = panelWidth / 1000.0f;
        int pagesHolderHeight = (int)(60 * pagesHolderScale);
        int pagesHolderWidth = (int)(pagesHolderHeight * (PAGES_HOLDER_TEXTURE_WIDTH / (float)PAGES_HOLDER_TEXTURE_HEIGHT));

        float contentPaddingTop = FRAME_CONTENT_PADDING_TOP * (panelHeight / 640.0f);
        float contentPaddingBottom = FRAME_CONTENT_PADDING_BOTTOM * (panelHeight / 640.0f);

        int listAreaHeight = (int)(playersPerPage * playerRowHeight);
        int availableSpace = panelHeight - (int)contentPaddingTop - (int)contentPaddingBottom - listAreaHeight;

        int pagesHolderX = panelX + (panelWidth - pagesHolderWidth) / 2;
        int pagesHolderY = panelY + (int)contentPaddingTop + listAreaHeight + (availableSpace - pagesHolderHeight) / 2;

        setupRenderSystem(PAGES_HOLDER_TEXTURE, fadeAlpha);
        RenderSystem.enableBlend();

        context.drawTexture(PAGES_HOLDER_TEXTURE, pagesHolderX, pagesHolderY, pagesHolderWidth, pagesHolderHeight,
            0.0f, 0.0f, PAGES_HOLDER_TEXTURE_WIDTH, PAGES_HOLDER_TEXTURE_HEIGHT, PAGES_HOLDER_TEXTURE_WIDTH, PAGES_HOLDER_TEXTURE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        int pageTextAlpha = (int)(fadeAlpha * 255);
        int pageTextColor = (pageTextAlpha << 24) | 0x3b1a17;

        float textScale = pagesHolderScale * 1.5f;
        int scaledTextWidth = (int)(textRenderer.getWidth(pageInfo) * textScale);
        int pageTextX = pagesHolderX + (pagesHolderWidth - scaledTextWidth) / 2;
        int pageTextY = pagesHolderY + (pagesHolderHeight - (int)(textRenderer.fontHeight * textScale)) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(pageTextX, pageTextY, 0);
        context.getMatrices().scale(textScale, textScale, 1.0f);
        context.drawText(textRenderer, pageInfo, 0, 0, pageTextColor, false);
        context.getMatrices().pop();
    }

    private void renderPlayerList(DrawContext context) {
        float contentPaddingTop = FRAME_CONTENT_PADDING_TOP * (panelHeight / 640.0f);
        float contentPaddingSides = FRAME_CONTENT_PADDING_SIDES * (panelWidth / 1000.0f);

        int startY = panelY + (int)contentPaddingTop;
        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, allPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * playerRowHeight;
            renderPlayerRow(context, player, panelX + (int)contentPaddingSides, rowY, (int)contentPaddingSides);
        }
    }

    private void renderPlayerRow(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y, int sidePadding) {
        float contentPaddingSides = FRAME_CONTENT_PADDING_SIDES * (panelWidth / 1000.0f);
        int rowWidth = panelWidth - (int)(contentPaddingSides * 2);
        int rowHeight = playerRowHeight - (int)(8 * uiScale);

        renderPlayerRowBackground(context, player, x, y, rowWidth, rowHeight);

        if (player.isMvp()) {
            renderMvpBadge(context, x, y, rowWidth);
        }

        renderPlayerHead(context, player, x, y, rowHeight);
        renderPlayerName(context, player, x, y);
        renderPlayerStats(context, player, x, y, rowWidth);
    }

    private void renderPlayerRowBackground(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y, int rowWidth, int rowHeight) {
        int rowBgAlpha = (int)(fadeAlpha * 90);
        int rowBg;
        if (player.isMvp()) {
            rowBg = (rowBgAlpha << 24) | 0x4a4020;
        } else {
            rowBg = ((int)(fadeAlpha * 80) << 24) | 0x2a2a4a;
        }
        context.fill(x, y, x + rowWidth, y + rowHeight, rowBg);
    }

    private void renderMvpBadge(DrawContext context, int x, int y, int rowWidth) {
        int crownHeight = (int)((isCompactMode ? 14 : 18) * uiScale);
        int crownWidth = (int)(crownHeight * 1.75f);
        int mvpBadgeX = x + rowWidth - crownWidth;
        int mvpBadgeY = y - crownHeight / 2;

        int badgeAlpha = (int)(fadeAlpha * 240);
        int badgeBg = (badgeAlpha << 24) | 0x8b7320;
        int badgeBorder = (badgeAlpha << 24) | MVP_GOLD_COLOR;

        context.fill(mvpBadgeX - 1, mvpBadgeY - 1, mvpBadgeX + crownWidth + 1, mvpBadgeY + crownHeight + 1, badgeBorder);
        context.fill(mvpBadgeX, mvpBadgeY, mvpBadgeX + crownWidth, mvpBadgeY + crownHeight, badgeBg);

        renderCrownIcon(context, mvpBadgeX, mvpBadgeY, crownWidth, crownHeight);
    }

    private void renderCrownIcon(DrawContext context, int badgeX, int badgeY, int badgeWidth, int badgeHeight) {
        int iconPadding = (int)(2 * uiScale);
        int iconHeight = badgeHeight - iconPadding * 2;
        int iconWidth = (int)(iconHeight * 1.75f);
        int iconX = badgeX + (badgeWidth - iconWidth) / 2;
        int iconY = badgeY + iconPadding;

        RenderSystem.setShaderTexture(0, CROWN_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
        RenderSystem.enableBlend();
        context.drawTexture(CROWN_TEXTURE, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private void renderPlayerHead(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y, int rowHeight) {
        int headX = x + (int)(6 * uiScale);
        int headY = y + (rowHeight - headSize) / 2;

        try {
            if (MinecraftClient.getInstance().getNetworkHandler() != null) {
                PlayerListEntry playerEntry = MinecraftClient.getInstance().getNetworkHandler()
                    .getPlayerList().stream()
                    .filter(entry -> entry.getProfile().getName().equals(player.playerName()))
                    .findFirst()
                    .orElse(null);

                if (playerEntry != null) {
                    Identifier skin = playerEntry.getSkinTexture();
                    context.drawTexture(skin, headX, headY, headSize, headSize, 8, 8, 8, 8, 64, 64);
                    context.drawTexture(skin, headX, headY, headSize, headSize, 40, 8, 8, 8, 64, 64);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void renderPlayerName(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y) {
        int headX = x + (int)(6 * uiScale);
        int textX = headX + headSize + (int)(10 * uiScale);
        int nameAlpha = (int)(fadeAlpha * 255);
        int nameColor = (nameAlpha << 24) | 0xFFFFFF;
        int nameY = y + (int)(4 * uiScale);

        if (isCompactMode) {
            float textScale = Math.max(0.7f, uiScale);
            context.getMatrices().push();
            context.getMatrices().translate(textX, nameY, 0);
            context.getMatrices().scale(textScale, textScale, 1.0f);
            context.drawText(textRenderer, player.playerName(), 0, 0, nameColor, true);
            context.getMatrices().pop();
        } else {
            context.drawText(textRenderer, player.playerName(), textX, nameY, nameColor, true);
        }
    }

    private void renderPlayerStats(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y, int rowWidth) {
        int headX = x + (int)(6 * uiScale);
        int textX = headX + headSize + (int)(10 * uiScale);

        BadgeDimensions badgeDims = calculateBadgeDimensions();
        float animProgress = getAnimationProgress();

        int badgeY1 = y + (int)((isCompactMode ? 12 : 16) * uiScale);
        int badgeY2 = badgeY1 + badgeDims.height + badgeDims.rowSpacing;

        int statsEndX = renderStatBadges(context, player, textX, badgeY1, badgeY2, badgeDims, animProgress);

        if (!player.achievements().isEmpty()) {
            int achievementX = statsEndX + badgeDims.spacing * 2;
            int maxWidth = x + rowWidth - achievementX - (int)(5 * uiScale);
            renderAchievements(context, achievementX, badgeY1, badgeY2, player.achievements(), badgeDims.textScale, maxWidth, badgeDims.height);
        }
    }

    private BadgeDimensions calculateBadgeDimensions() {
        if (isCompactMode) {
            return new BadgeDimensions(
                Math.max(8, (int)(8 * uiScale)),
                Math.max(2, (int)(2 * uiScale)),
                Math.max(2, (int)(2 * uiScale)),
                Math.max(1, (int)(1 * uiScale)),
                Math.max(0.5f, uiScale * 0.75f)
            );
        } else {
            return new BadgeDimensions(
                Math.max(12, (int)(14 * uiScale)),
                (int)(5 * uiScale),
                (int)(4 * uiScale),
                (int)(3 * uiScale),
                1.0f
            );
        }
    }

    private record BadgeDimensions(int height, int padding, int spacing, int rowSpacing, float textScale) {}

    private int renderStatBadges(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int startX, int badgeY1, int badgeY2, BadgeDimensions dims, float animProgress) {
        int animBlocks = (int)(player.blocksDestroyed() * animProgress);
        int animDistance = (int)(player.distanceWalked() * animProgress / 100);
        int animMobs = (int)(player.mobsKilled() * animProgress);
        int animDeaths = (int)(player.deaths() * animProgress);
        int animJumps = (int)(player.jumps() * animProgress);

        String blocksText = Text.translatable("midnightthoughts.summary.blocks").getString() + " " + animBlocks;
        String distanceText = Text.translatable("midnightthoughts.summary.distance").getString() + " " + animDistance;
        String mobsText = Text.translatable("midnightthoughts.summary.mobs").getString() + " " + animMobs;
        String deathsText = Text.translatable("midnightthoughts.summary.deaths").getString() + " " + animDeaths;
        String jumpsText = Text.translatable("midnightthoughts.summary.jumps").getString() + " " + animJumps;

        int currentX = startX;
        currentX = renderBadge(context, currentX, badgeY1, dims.height, dims.padding, blocksText, 0x3d5a80, dims.textScale);
        currentX += dims.spacing;
        currentX = renderBadge(context, currentX, badgeY1, dims.height, dims.padding, distanceText, 0x2a6041, dims.textScale);
        currentX += dims.spacing;
        int statsEndX = renderBadge(context, currentX, badgeY1, dims.height, dims.padding, mobsText, 0x7a3d3d, dims.textScale);

        currentX = startX;
        currentX = renderBadge(context, currentX, badgeY2, dims.height, dims.padding, deathsText, 0x4a3d5a, dims.textScale);
        currentX += dims.spacing;
        renderBadge(context, currentX, badgeY2, dims.height, dims.padding, jumpsText, 0x5a4a3d, dims.textScale);

        return statsEndX;
    }

    private float getAnimationProgress() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed < 0) return 0.0f;
        float progress = Math.min(1.0f, elapsed / (float) STAT_ANIMATION_DURATION);
        return easeOutQuad(progress);
    }

    private float easeOutQuad(float t) {
        return t * (2 - t);
    }

    private void renderAchievements(DrawContext context, int x, int y1, int y2, List<String> achievements, float textScale, int maxWidth, int badgeHeight) {
        int padding = (int)(3 * uiScale);
        int spacing = (int)(3 * uiScale);
        int maxAchievements = isCompactMode ? 2 : 4;

        int currentX = x;
        int currentY = y1;
        int count = 0;

        for (String achievementId : achievements) {
            if (count >= maxAchievements) break;

            String achievementText = Text.translatable("midnightthoughts.achievement." + achievementId).getString();
            int textWidth = (int)(textRenderer.getWidth(achievementText) * textScale);
            int badgeWidth = textWidth + padding * 2;

            if (shouldMoveToNextRow(currentX, badgeWidth, x, maxWidth, currentY, y1)) {
                currentX = x;
                currentY = y2;
            }

            if (shouldStopRendering(currentX, badgeWidth, x, maxWidth, currentY, y2)) {
                break;
            }

            renderAchievementBadge(context, currentX, currentY, badgeWidth, badgeHeight, padding, achievementText, textScale);
            achievementAreas.add(new AchievementTooltipArea(currentX, currentY, badgeWidth, badgeHeight, achievementId));

            currentX += badgeWidth + spacing;
            count++;
        }
    }

    private boolean shouldMoveToNextRow(int currentX, int badgeWidth, int x, int maxWidth, int currentY, int y1) {
        return currentX + badgeWidth > x + maxWidth && currentY == y1;
    }

    private boolean shouldStopRendering(int currentX, int badgeWidth, int x, int maxWidth, int currentY, int y2) {
        return currentX + badgeWidth > x + maxWidth && currentY == y2;
    }

    private void renderAchievementBadge(DrawContext context, int x, int y, int width, int height, int padding, String text, float textScale) {
        int alpha = (int)(fadeAlpha * 200);
        int bgColor = (alpha << 24) | 0x8a6a2a;
        context.fill(x, y, x + width, y + height, bgColor);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
        int textY = y + (height - (int)(8 * textScale)) / 2;

        renderScaledText(context, text, x + padding, textY, textColor, textScale, false);
    }

    private void renderAchievementTooltips(DrawContext context, int mouseX, int mouseY) {
        for (AchievementTooltipArea area : achievementAreas) {
            if (isMouseOverArea(mouseX, mouseY, area)) {
                renderTooltip(context, mouseX, mouseY, area.achievementId);
                break;
            }
        }
    }

    private boolean isMouseOverArea(int mouseX, int mouseY, AchievementTooltipArea area) {
        return mouseX >= area.x && mouseX <= area.x + area.width &&
               mouseY >= area.y && mouseY <= area.y + area.height;
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY, String achievementId) {
        String tooltipKey = "midnightthoughts.achievement." + achievementId + ".desc";
        Text tooltipText = Text.translatable(tooltipKey);

        TooltipDimensions tooltipDims = calculateTooltipDimensions(tooltipText, mouseX, mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400);

        renderTooltipBackground(context, tooltipDims);
        renderTooltipText(context, tooltipText, tooltipDims);

        context.getMatrices().pop();
    }

    private TooltipDimensions calculateTooltipDimensions(Text text, int mouseX, int mouseY) {
        int tooltipPadding = 4;
        int tooltipWidth = textRenderer.getWidth(text) + tooltipPadding * 2;
        int tooltipHeight = 12;

        int tooltipX = mouseX + 8;
        int tooltipY = mouseY - tooltipHeight - 4;

        if (tooltipX + tooltipWidth > width) {
            tooltipX = mouseX - tooltipWidth - 8;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 16;
        }

        return new TooltipDimensions(tooltipX, tooltipY, tooltipWidth, tooltipHeight, tooltipPadding);
    }

    private void renderTooltipBackground(DrawContext context, TooltipDimensions dims) {
        int bgAlpha = (int)(fadeAlpha * 240);
        int bgColor = (bgAlpha << 24) | 0x1a1a2e;
        int borderColor = (bgAlpha << 24) | 0x8a6a2a;

        context.fill(dims.x - 1, dims.y - 1, dims.x + dims.width + 1, dims.y + dims.height + 1, borderColor);
        context.fill(dims.x, dims.y, dims.x + dims.width, dims.y + dims.height, bgColor);
    }

    private void renderTooltipText(DrawContext context, Text text, TooltipDimensions dims) {
        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
        context.drawText(textRenderer, text, dims.x + dims.padding, dims.y + 2, textColor, false);
    }

    private record TooltipDimensions(int x, int y, int width, int height, int padding) {}

    private record AchievementTooltipArea(int x, int y, int width, int height, String achievementId) {}

    private int renderBadge(DrawContext context, int x, int y, int height, int padding, String text, int bgColor, float textScale) {
        int textWidth = (int)(textRenderer.getWidth(text) * textScale);
        int badgeWidth = textWidth + padding * 2;

        int alpha = (int)(fadeAlpha * 180);
        int bg = (alpha << 24) | bgColor;
        context.fill(x, y, x + badgeWidth, y + height, bg);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xeeeeee;
        int textY = y + (height - (int)(8 * textScale)) / 2;

        renderScaledText(context, text, x + padding, textY, textColor, textScale, false);

        return x + badgeWidth;
    }

    private void renderScaledText(DrawContext context, String text, int x, int y, int color, float scale, boolean shadow) {
        if (scale < 1.0f) {
            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawText(textRenderer, text, 0, 0, color, shadow);
            context.getMatrices().pop();
        } else {
            context.drawText(textRenderer, text, x, y, color, shadow);
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

    private static class StyledButtonWidget extends ButtonWidget {
        public StyledButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;

            boolean hovered = isMouseOver(mouseX, mouseY);

            int bgColor = hovered ? 0xDD6a5a8a : 0xCC4a3a6a;
            int borderColor = hovered ? 0xFFaa8acc : 0xFF8a6a9a;

            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
            context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
            context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
            context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
            context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

            int textColor = hovered ? 0xFFeeddff : 0xFFddccee;
            int textX = getX() + (getWidth() - textRenderer.getWidth(getMessage())) / 2;
            int textY = getY() + (getHeight() - 8) / 2;
            context.drawText(textRenderer, getMessage(), textX, textY, textColor, true);
        }
    }
}