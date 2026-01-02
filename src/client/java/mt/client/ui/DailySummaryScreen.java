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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/background.png");
    private static final Identifier CROWN_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/crown.png");

    private static final long STAT_ANIMATION_DURATION = 800;
    private static final int MVP_GOLD_COLOR = 0xFFD700;
    private static final int MVP_BORDER_COLOR = 0xDAA520;

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
        this.allPlayers = players;
        this.screenOpenTime = System.currentTimeMillis();
        this.animationStartTime = System.currentTimeMillis() + 300;
        int totalPages = Math.max(1, (int) Math.ceil((double) players.size() / 4));
        LOGGER.info("[DailySummaryScreen] Created with {} players, {} pages", players.size(), totalPages);
    }

    private void calculateDimensions() {
        isCompactMode = height < 350 || width < 500;

        if (isCompactMode) {
            uiScale = Math.min((float)width / 420.0f, (float)height / 300.0f);
            uiScale = Math.max(0.4f, Math.min(1.0f, uiScale));
            playersPerPage = 3;

            panelWidth = Math.min(width - 20, (int)(320 * uiScale));
            panelHeight = Math.min(height - 50, (int)(190 * uiScale));
            playerRowHeight = (int)(45 * uiScale);
            headSize = (int)(20 * uiScale);
        } else {
            uiScale = Math.min((float)width / 854.0f, (float)height / 480.0f);
            uiScale = Math.max(0.6f, Math.min(1.5f, uiScale));
            playersPerPage = 4;

            panelWidth = (int)(480 * uiScale);
            panelHeight = (int)(320 * uiScale);
            playerRowHeight = (int)(60 * uiScale);
            headSize = (int)(32 * uiScale);
        }

        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    @Override
    protected void init() {
        super.init();
        calculateDimensions();
        LOGGER.info("[DailySummaryScreen] init() - compact: {}, scale: {}, panel: {}x{}",
            isCompactMode, uiScale, panelWidth, panelHeight);

        int buttonWidth = isCompactMode ? Math.max(60, (int)(70 * uiScale)) : (int)(90 * uiScale);
        int buttonHeight = isCompactMode ? Math.max(16, (int)(16 * uiScale)) : (int)(20 * uiScale);
        int buttonY = panelY + panelHeight + (int)(6 * uiScale);
        int buttonSpacing = (int)(6 * uiScale);

        int recalculatedPages = Math.max(1, (int) Math.ceil((double) allPlayers.size() / playersPerPage));

        if (recalculatedPages > 1) {
            int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
            int navStartX = width / 2 - navButtonsWidth / 2;

            addDrawableChild(new StyledButtonWidget(
                navStartX, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.previous"),
                button -> {
                    if (currentPage > 0) {
                        currentPage--;
                        clearAndInit();
                    }
                }
            ));

            addDrawableChild(new StyledButtonWidget(
                navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
                Text.translatable("midnightthoughts.summary.next"),
                button -> {
                    if (currentPage < recalculatedPages - 1) {
                        currentPage++;
                        clearAndInit();
                    }
                }
            ));

            buttonY += buttonHeight + buttonSpacing;
        }

        addDrawableChild(new StyledButtonWidget(
            width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight,
            Text.translatable("midnightthoughts.summary.continue"),
            button -> {
                LOGGER.info("[DailySummaryScreen] Continue button pressed");
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

        calculateDimensions();
        renderBackgroundImage(context);
        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);

        renderAchievementTooltips(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void renderBackgroundImage(DrawContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        float bgAspect = 16.0f / 9.0f;
        float screenAspect = (float)width / (float)height;

        int bgWidth, bgHeight, bgX, bgY;
        if (screenAspect > bgAspect) {
            bgWidth = width;
            bgHeight = (int)(width / bgAspect);
            bgX = 0;
            bgY = (height - bgHeight) / 2;
        } else {
            bgHeight = height;
            bgWidth = (int)(height * bgAspect);
            bgX = (width - bgWidth) / 2;
            bgY = 0;
        }

        context.drawTexture(BACKGROUND_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);
    }

    private void renderPanel(DrawContext context) {
        int panelAlpha = (int)(fadeAlpha * 220);
        int panelBg = (panelAlpha << 24) | 0x1a1a2e;
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, panelBg);

        int borderAlpha = (int)(fadeAlpha * 255);
        int borderColor1 = (borderAlpha << 24) | 0x6a5a8a;
        int borderColor2 = (borderAlpha << 24) | 0x8a6a9a;
        int borderThickness = Math.max(1, (int)(2 * uiScale));

        context.fill(panelX, panelY, panelX + panelWidth, panelY + borderThickness, borderColor1);
        context.fill(panelX, panelY + panelHeight - borderThickness, panelX + panelWidth, panelY + panelHeight, borderColor2);
        context.fill(panelX, panelY, panelX + borderThickness, panelY + panelHeight, borderColor1);
        context.fill(panelX + panelWidth - borderThickness, panelY, panelX + panelWidth, panelY + panelHeight, borderColor2);

        int cornerSize = Math.max(4, (int)(6 * uiScale));
        int cornerColor = (borderAlpha << 24) | 0xaa8acc;
        context.fill(panelX, panelY, panelX + cornerSize, panelY + cornerSize, cornerColor);
        context.fill(panelX + panelWidth - cornerSize, panelY, panelX + panelWidth, panelY + cornerSize, cornerColor);
        context.fill(panelX, panelY + panelHeight - cornerSize, panelX + cornerSize, panelY + panelHeight, cornerColor);
        context.fill(panelX + panelWidth - cornerSize, panelY + panelHeight - cornerSize, panelX + panelWidth, panelY + panelHeight, cornerColor);

        Text title = Text.translatable("midnightthoughts.summary.title");
        int titleColor = (borderAlpha << 24) | 0xeeddff;
        int titleX = panelX + panelWidth / 2 - textRenderer.getWidth(title) / 2;
        int titleY = panelY + (int)(8 * uiScale);
        context.drawText(textRenderer, title, titleX, titleY, titleColor, true);

        int lineY = panelY + (int)(22 * uiScale);
        int lineColor = (int)(fadeAlpha * 120) << 24 | 0x8a7aaa;
        int linePadding = (int)(10 * uiScale);
        context.fill(panelX + linePadding, lineY, panelX + panelWidth - linePadding, lineY + 1, lineColor);

        int recalculatedPages = Math.max(1, (int) Math.ceil((double) allPlayers.size() / playersPerPage));
        if (recalculatedPages > 1) {
            Text pageInfo = Text.translatable("midnightthoughts.summary.page", currentPage + 1, recalculatedPages);
            int pageColor = (int)(fadeAlpha * 180) << 24 | 0xccbbdd;
            int pageX = panelX + panelWidth / 2 - textRenderer.getWidth(pageInfo) / 2;
            context.drawText(textRenderer, pageInfo, pageX, lineY + (int)(4 * uiScale), pageColor, false);
        }
    }

    private void renderPlayerList(DrawContext context) {
        int startY = panelY + (int)(38 * uiScale);
        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, allPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * playerRowHeight;
            renderPlayerRow(context, player, panelX + (int)(10 * uiScale), rowY);
        }
    }

    private void renderPlayerRow(DrawContext context, DailySummaryPacket.PlayerDailySummary player, int x, int y) {
        int rowPadding = (int)(10 * uiScale);
        int rowWidth = panelWidth - rowPadding * 2;
        int rowHeight = playerRowHeight - (int)(4 * uiScale);

        int rowBgAlpha = (int)(fadeAlpha * 90);
        int rowBg;
        if (player.isMvp()) {
            rowBg = (rowBgAlpha << 24) | 0x4a4020;
        } else {
            rowBg = ((int)(fadeAlpha * 80) << 24) | 0x2a2a4a;
        }
        context.fill(x, y, x + rowWidth, y + rowHeight, rowBg);

        if (player.isMvp()) {
            int crownHeight = (int)((isCompactMode ? 14 : 18) * uiScale);
            int crownWidth = (int)(crownHeight * 1.75f);
            int mvpBadgeX = x + rowWidth - crownWidth;
            int mvpBadgeY = y - crownHeight / 2;

            int badgeAlpha = (int)(fadeAlpha * 240);
            int badgeBg = (badgeAlpha << 24) | 0x8b7320;
            int badgeBorder = (badgeAlpha << 24) | MVP_GOLD_COLOR;

            context.fill(mvpBadgeX - 1, mvpBadgeY - 1, mvpBadgeX + crownWidth + 1, mvpBadgeY + crownHeight + 1, badgeBorder);
            context.fill(mvpBadgeX, mvpBadgeY, mvpBadgeX + crownWidth, mvpBadgeY + crownHeight, badgeBg);

            int iconPadding = (int)(2 * uiScale);
            int iconHeight = crownHeight - iconPadding * 2;
            int iconWidth = (int)(iconHeight * 1.75f);
            int iconX = mvpBadgeX + (crownWidth - iconWidth) / 2;
            int iconY = mvpBadgeY + iconPadding;

            RenderSystem.setShaderTexture(0, CROWN_TEXTURE);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fadeAlpha);
            RenderSystem.enableBlend();
            context.drawTexture(CROWN_TEXTURE, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }

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
                    Identifier skin = playerEntry.getSkinTextures().texture();
                    context.drawTexture(skin, headX, headY, headSize, headSize, 8, 8, 8, 8, 64, 64);
                    context.drawTexture(skin, headX, headY, headSize, headSize, 40, 8, 8, 8, 64, 64);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[DailySummaryScreen] Failed to render player head: {}", e.getMessage());
        }

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

        float animProgress = getAnimationProgress();

        int badgeHeight;
        int badgePadding;
        int badgeSpacing;
        int badgeRowSpacing;
        float badgeTextScale;

        if (isCompactMode) {
            badgeHeight = Math.max(9, (int)(10 * uiScale));
            badgePadding = Math.max(2, (int)(3 * uiScale));
            badgeSpacing = Math.max(2, (int)(2 * uiScale));
            badgeRowSpacing = Math.max(1, (int)(2 * uiScale));
            badgeTextScale = Math.max(0.6f, uiScale * 0.85f);
        } else {
            badgeHeight = Math.max(12, (int)(14 * uiScale));
            badgePadding = (int)(5 * uiScale);
            badgeSpacing = (int)(4 * uiScale);
            badgeRowSpacing = (int)(3 * uiScale);
            badgeTextScale = 1.0f;
        }

        int badgeY1 = y + (int)((isCompactMode ? 12 : 16) * uiScale);
        int badgeY2 = badgeY1 + badgeHeight + badgeRowSpacing;

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

        int currentX = textX;
        currentX = renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, blocksText, 0x3d5a80, badgeTextScale);
        currentX += badgeSpacing;
        currentX = renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, distanceText, 0x2a6041, badgeTextScale);
        currentX += badgeSpacing;
        int statsEndX = renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, mobsText, 0x7a3d3d, badgeTextScale);

        currentX = textX;
        currentX = renderBadge(context, currentX, badgeY2, badgeHeight, badgePadding, deathsText, 0x4a3d5a, badgeTextScale);
        currentX += badgeSpacing;
        renderBadge(context, currentX, badgeY2, badgeHeight, badgePadding, jumpsText, 0x5a4a3d, badgeTextScale);

        if (!player.achievements().isEmpty()) {
            int achievementX = statsEndX + badgeSpacing * 2;
            int achievementY = badgeY1;
            int maxWidth = x + rowWidth - achievementX - (int)(5 * uiScale);
            renderAchievements(context, achievementX, achievementY, badgeY2, player.achievements(), badgeTextScale, maxWidth, badgeHeight);
        }
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
        int achievementHeight = badgeHeight;
        int padding = (int)(3 * uiScale);
        int spacing = (int)(3 * uiScale);

        int currentX = x;
        int currentY = y1;
        int count = 0;
        int maxAchievements = isCompactMode ? 2 : 4;

        for (String achievementId : achievements) {
            if (count >= maxAchievements) break;

            String achievementText = Text.translatable("midnightthoughts.achievement." + achievementId).getString();
            int textWidth = (int)(textRenderer.getWidth(achievementText) * textScale);
            int badgeWidth = textWidth + padding * 2;

            if (currentX + badgeWidth > x + maxWidth && currentY == y1) {
                currentX = x;
                currentY = y2;
            }

            if (currentX + badgeWidth > x + maxWidth && currentY == y2) {
                break;
            }

            int alpha = (int)(fadeAlpha * 200);
            int bgColor = (alpha << 24) | 0x8a6a2a;

            context.fill(currentX, currentY, currentX + badgeWidth, currentY + achievementHeight, bgColor);

            int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
            int textY = currentY + (achievementHeight - (int)(8 * textScale)) / 2;

            if (textScale < 1.0f) {
                context.getMatrices().push();
                context.getMatrices().translate(currentX + padding, textY, 0);
                context.getMatrices().scale(textScale, textScale, 1.0f);
                context.drawText(textRenderer, achievementText, 0, 0, textColor, false);
                context.getMatrices().pop();
            } else {
                context.drawText(textRenderer, achievementText, currentX + padding, textY, textColor, false);
            }

            achievementAreas.add(new AchievementTooltipArea(
                currentX, currentY, badgeWidth, achievementHeight, achievementId
            ));

            currentX += badgeWidth + spacing;
            count++;
        }
    }

    private void renderAchievementTooltips(DrawContext context, int mouseX, int mouseY) {
        for (AchievementTooltipArea area : achievementAreas) {
            if (mouseX >= area.x && mouseX <= area.x + area.width &&
                mouseY >= area.y && mouseY <= area.y + area.height) {

                String tooltipKey = "midnightthoughts.achievement." + area.achievementId + ".desc";
                Text tooltipText = Text.translatable(tooltipKey);

                int tooltipPadding = 4;
                int tooltipWidth = textRenderer.getWidth(tooltipText) + tooltipPadding * 2;
                int tooltipHeight = 12;

                int tooltipX = mouseX + 8;
                int tooltipY = mouseY - tooltipHeight - 4;

                if (tooltipX + tooltipWidth > width) {
                    tooltipX = mouseX - tooltipWidth - 8;
                }
                if (tooltipY < 0) {
                    tooltipY = mouseY + 16;
                }

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 400);

                int bgAlpha = (int)(fadeAlpha * 240);
                int bgColor = (bgAlpha << 24) | 0x1a1a2e;
                int borderColor = (bgAlpha << 24) | 0x8a6a2a;

                context.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, borderColor);
                context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, bgColor);

                int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
                context.drawText(textRenderer, tooltipText, tooltipX + tooltipPadding, tooltipY + 2, textColor, false);

                context.getMatrices().pop();

                break;
            }
        }
    }

    private record AchievementTooltipArea(int x, int y, int width, int height, String achievementId) {}

    private int renderBadge(DrawContext context, int x, int y, int height, int padding, String text, int bgColor, float textScale) {
        int textWidth = (int)(textRenderer.getWidth(text) * textScale);
        int badgeWidth = textWidth + padding * 2;

        int alpha = (int)(fadeAlpha * 180);
        int bg = (alpha << 24) | bgColor;

        context.fill(x, y, x + badgeWidth, y + height, bg);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xeeeeee;
        int textY = y + (height - (int)(8 * textScale)) / 2;

        if (textScale < 1.0f) {
            context.getMatrices().push();
            context.getMatrices().translate(x + padding, textY, 0);
            context.getMatrices().scale(textScale, textScale, 1.0f);
            context.drawText(textRenderer, text, 0, 0, textColor, false);
            context.getMatrices().pop();
        } else {
            context.drawText(textRenderer, text, x + padding, textY, textColor, false);
        }

        return x + badgeWidth;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        LOGGER.info("[DailySummaryScreen] Screen closed");
        super.close();
    }

    private static class StyledButtonWidget extends ButtonWidget {
        public StyledButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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