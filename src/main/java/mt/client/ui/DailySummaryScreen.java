package mt.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import mt.client.MidnightThoughtsClient;
import mt.client.network.ClientNetworkHandler;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/background.png");
    private static final ResourceLocation CROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MidnightThoughtsClient.MOD_ID, "textures/gui/crown.png");
    private static final long STAT_ANIMATION_DURATION = 800;
    private static final int MVP_GOLD_COLOR = 0xFFD700;

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
    private Font fontRenderer;

    public DailySummaryScreen(List<DailySummaryPacket.PlayerDailySummary> players) {
        super(Component.literal("Summary"));
        this.allPlayers = players;
        this.screenOpenTime = System.currentTimeMillis();
        this.animationStartTime = System.currentTimeMillis() + 300;
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
        fontRenderer = Minecraft.getInstance().font;
        calculateDimensions();
        int buttonWidth = isCompactMode ? Math.max(60, (int)(70 * uiScale)) : (int)(90 * uiScale);
        int buttonHeight = isCompactMode ? Math.max(16, (int)(16 * uiScale)) : (int)(20 * uiScale);
        int buttonY = panelY + panelHeight + (int)(6 * uiScale);
        int buttonSpacing = (int)(6 * uiScale);
        int recalculatedPages = Math.max(1, (int) Math.ceil((double) allPlayers.size() / playersPerPage));
        if (recalculatedPages > 1) {
            int navButtonsWidth = buttonWidth * 2 + buttonSpacing;
            int navStartX = width / 2 - navButtonsWidth / 2;
            addRenderableWidget(new StyledButtonWidget(
                navStartX, buttonY, buttonWidth, buttonHeight,
                Component.translatable("midnightthoughts.summary.previous"),
                b -> {
                    if (currentPage > 0) {
                        currentPage--;
                        this.init();
                    }
                },
                fontRenderer
            ));
            addRenderableWidget(new StyledButtonWidget(
                navStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight,
                Component.translatable("midnightthoughts.summary.next"),
                b -> {
                    if (currentPage < recalculatedPages - 1) {
                        currentPage++;
                        this.init();
                    }
                },
                fontRenderer
            ));
            buttonY += buttonHeight + buttonSpacing;
        }
        addRenderableWidget(new StyledButtonWidget(
            width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight,
            Component.translatable("midnightthoughts.summary.continue"),
            b -> {
                ClientNetworkHandler.sendSummaryAcknowledge();
                onClose();
            },
            fontRenderer
        ));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
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

    private void renderBackgroundImage(GuiGraphics context) {
        RenderSystem.setShader(() -> GameRenderer.getPositionTexShader());
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

        context.blit(BACKGROUND_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);
    }

    private void renderPanel(GuiGraphics context) {
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

        Component title = Component.translatable("midnightthoughts.summary.title");
        int titleColor = (borderAlpha << 24) | 0xeeddff;
        int titleX = panelX + panelWidth / 2 - fontRenderer.width(title) / 2;
        int titleY = panelY + (int)(8 * uiScale);
        fontRenderer.drawInBatch(title.getString(), titleX, titleY, titleColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

        int lineY = panelY + (int)(22 * uiScale);
        int lineColor = (int)(fadeAlpha * 120) << 24 | 0x8a7aaa;
        int linePadding = (int)(10 * uiScale);
        context.fill(panelX + linePadding, lineY, panelX + panelWidth - linePadding, lineY + 1, lineColor);

        int recalculatedPages = Math.max(1, (int) Math.ceil((double) allPlayers.size() / playersPerPage));
        if (recalculatedPages > 1) {
            Component pageInfo = Component.translatable("midnightthoughts.summary.page", currentPage + 1, recalculatedPages);
            int pageColor = (int)(fadeAlpha * 180) << 24 | 0xccbbdd;
            int pageX = panelX + panelWidth / 2 - fontRenderer.width(pageInfo) / 2;
            fontRenderer.drawInBatch(pageInfo.getString(), pageX, lineY + (int)(4 * uiScale), pageColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    private void renderPlayerList(GuiGraphics context) {
        int startY = panelY + (int)(38 * uiScale);
        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, allPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            DailySummaryPacket.PlayerDailySummary player = allPlayers.get(i);
            int rowY = startY + (i - startIndex) * playerRowHeight;
            renderPlayerRow(context, player, panelX + (int)(10 * uiScale), rowY);
        }
    }

    private void renderPlayerRow(GuiGraphics context, DailySummaryPacket.PlayerDailySummary player, int x, int y) {
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
            context.blit(CROWN_TEXTURE, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
        int headX = x + (int)(6 * uiScale);
        int headY = y + (rowHeight - headSize) / 2;
        try {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                var info = connection.getPlayerInfo(player.playerName());
                if (info != null) {
                    ResourceLocation skin = info.getSkinLocation();
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderTexture(0, skin);
                    context.blit(skin, headX, headY, headSize, headSize, 8.0f, 8.0f, 8, 8, 64, 64);
                    context.blit(skin, headX, headY, headSize, headSize, 40.0f, 8.0f, 8, 8, 64, 64);
                    RenderSystem.disableBlend();
                }
            }
        } catch (Exception ignored) {}

        int textX = headX + headSize + (int)(10 * uiScale);
        int nameAlpha = (int)(fadeAlpha * 255);
        int nameColor = (nameAlpha << 24) | 0xFFFFFF;
        int nameY = y + (int)(4 * uiScale);
        if (isCompactMode) {
            float textScale = Math.max(0.7f, uiScale);
            context.pose().pushPose();
            context.pose().translate(textX, nameY, 0);
            context.pose().scale(textScale, textScale, 1.0f);
            fontRenderer.drawInBatch(player.playerName(), 0, 0, nameColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            context.pose().popPose();
        } else {
            fontRenderer.drawInBatch(player.playerName(), textX, nameY, nameColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
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

        String blocksText = Component.translatable("midnightthoughts.summary.blocks").getString() + " " + animBlocks;
        String distanceText = Component.translatable("midnightthoughts.summary.distance").getString() + " " + animDistance;
        String mobsText = Component.translatable("midnightthoughts.summary.mobs").getString() + " " + animMobs;
        String deathsText = Component.translatable("midnightthoughts.summary.deaths").getString() + " " + animDeaths;
        String jumpsText = Component.translatable("midnightthoughts.summary.jumps").getString() + " " + animJumps;

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
            int maxWidth = x + rowWidth - achievementX - (int)(5 * uiScale);
            renderAchievements(context, achievementX, badgeY1, badgeY2, player.achievements(), badgeTextScale, maxWidth, badgeHeight);
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

    private void renderAchievements(GuiGraphics context, int x, int y1, int y2, List<String> achievements, float textScale, int maxWidth, int badgeHeight) {
        int padding = (int)(3 * uiScale);
        int spacing = (int)(3 * uiScale);

        int currentX = x;
        int currentY = y1;
        int count = 0;
        int maxAchievements = isCompactMode ? 2 : 4;

        for (String achievementId : achievements) {
            if (count >= maxAchievements) break;

            String achievementText = Component.translatable("midnightthoughts.achievement." + achievementId).getString();
            int textWidth = (int)(fontRenderer.width(achievementText) * textScale);
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

            context.fill(currentX, currentY, currentX + badgeWidth, currentY + badgeHeight, bgColor);

            int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
            int textY = currentY + (badgeHeight - (int)(8 * textScale)) / 2;

            if (textScale < 1.0f) {
                context.pose().pushPose();
                context.pose().translate(currentX + padding, textY, 0);
                context.pose().scale(textScale, textScale, 1.0f);
                fontRenderer.drawInBatch(achievementText, 0, 0, textColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
                context.pose().popPose();
            } else {
                fontRenderer.drawInBatch(achievementText, currentX + padding, textY, textColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            }

            achievementAreas.add(new AchievementTooltipArea(
                currentX, currentY, badgeWidth, badgeHeight, achievementId
            ));

            currentX += badgeWidth + spacing;
            count++;
        }
    }

    private void renderAchievementTooltips(GuiGraphics context, int mouseX, int mouseY) {
        for (AchievementTooltipArea area : achievementAreas) {
            if (mouseX >= area.x && mouseX <= area.x + area.width &&
                mouseY >= area.y && mouseY <= area.y + area.height) {

                Component tooltipText = Component.translatable("midnightthoughts.achievement." + area.achievementId + ".desc");

                int tooltipPadding = 4;
                int tooltipWidth = fontRenderer.width(tooltipText) + tooltipPadding * 2;
                int tooltipHeight = 12;

                int tooltipX = mouseX + 8;
                int tooltipY = mouseY - tooltipHeight - 4;

                if (tooltipX + tooltipWidth > width) {
                    tooltipX = mouseX - tooltipWidth - 8;
                }
                if (tooltipY < 0) {
                    tooltipY = mouseY + 16;
                }

                context.pose().pushPose();
                context.pose().translate(0, 0, 400);

                int bgAlpha = (int)(fadeAlpha * 240);
                int bgColor = (bgAlpha << 24) | 0x1a1a2e;
                int borderColor = (bgAlpha << 24) | 0x8a6a2a;

                context.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, borderColor);
                context.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, bgColor);

                int textColor = ((int)(fadeAlpha * 255) << 24) | 0xffd700;
                fontRenderer.drawInBatch(tooltipText.getString(), tooltipX + tooltipPadding, tooltipY + 2, textColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

                context.pose().popPose();

                break;
            }
        }
    }

    private record AchievementTooltipArea(int x, int y, int width, int height, String achievementId) {}

    private int renderBadge(GuiGraphics context, int x, int y, int height, int padding, String text, int bgColor, float textScale) {
        int textWidth = (int)(fontRenderer.width(text) * textScale);
        int badgeWidth = textWidth + padding * 2;

        int alpha = (int)(fadeAlpha * 180);
        int bg = (alpha << 24) | bgColor;

        context.fill(x, y, x + badgeWidth, y + height, bg);

        int textColor = ((int)(fadeAlpha * 255) << 24) | 0xeeeeee;
        int textY = y + (height - (int)(8 * textScale)) / 2;
        if (textScale < 1.0f) {
            context.pose().pushPose();
            context.pose().translate(x + padding, textY, 0);
            context.pose().scale(textScale, textScale, 1.0f);
            fontRenderer.drawInBatch(text, 0, 0, textColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            context.pose().popPose();
        } else {
            fontRenderer.drawInBatch(text, x + padding, textY, textColor, false, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        }

        return x + badgeWidth;
    }

    public boolean shouldPause() {
        return false;
    }

    public void onClose() {
        super.onClose();
    }

    private static class StyledButtonWidget extends Button {
        private final Font fontRenderer;
        public StyledButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress, Font fontRenderer) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.fontRenderer = fontRenderer;
        }
        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            boolean hovered = isHoveredOrFocused();
            int bgColor = hovered ? 0xDD6a5a8a : 0xCC4a3a6a;
            int borderColor = hovered ? 0xFFaa8acc : 0xFF8a6a9a;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
            context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
            context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
            context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
            context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);
            int textColor = hovered ? 0xFFeeddff : 0xFFddccee;
            int textX = getX() + (getWidth() - fontRenderer.width(getMessage())) / 2;
            int textY = getY() + (getHeight() - 8) / 2;
            fontRenderer.drawInBatch(getMessage().getString(), textX, textY, textColor, true, context.pose().last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }
}
