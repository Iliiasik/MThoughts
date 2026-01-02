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

import java.util.List;

public class DailySummaryScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(MidnightThoughtsClient.MOD_ID, "textures/gui/background.png");

    private final List<DailySummaryPacket.PlayerDailySummary> allPlayers;
    private int currentPage = 0;
    private float fadeAlpha = 0.0f;
    private final long screenOpenTime;

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

        calculateDimensions();
        renderBackgroundImage(context);
        renderPanel(context);
        renderPlayerList(context);

        super.render(context, mouseX, mouseY, delta);
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

        int rowBgAlpha = (int)(fadeAlpha * 80);
        int rowBg = (rowBgAlpha << 24) | 0x2a2a4a;
        context.fill(x, y, x + rowWidth, y + rowHeight, rowBg);

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
        int nameY = y + (int)(3 * uiScale);

        if (isCompactMode) {
            context.getMatrices().push();
            context.getMatrices().translate(textX, nameY, 0);
            float textScale = Math.max(0.7f, uiScale);
            context.getMatrices().scale(textScale, textScale, 1.0f);
            context.drawText(textRenderer, player.playerName(), 0, 0, nameColor, true);
            context.getMatrices().pop();
        } else {
            context.drawText(textRenderer, player.playerName(), textX, nameY, nameColor, true);
        }

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

        int distanceMeters = player.distanceWalked() / 100;

        String blocksText = Text.translatable("midnightthoughts.summary.blocks").getString() + " " + player.blocksDestroyed();
        String distanceText = Text.translatable("midnightthoughts.summary.distance").getString() + " " + distanceMeters + "m";
        String mobsText = Text.translatable("midnightthoughts.summary.mobs").getString() + " " + player.mobsKilled();
        String deathsText = Text.translatable("midnightthoughts.summary.deaths").getString() + " " + player.deaths();
        String jumpsText = Text.translatable("midnightthoughts.summary.jumps").getString() + " " + player.jumps();

        int currentX = textX;
        currentX = renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, blocksText, 0x3d5a80, badgeTextScale);
        currentX += badgeSpacing;
        currentX = renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, distanceText, 0x2a6041, badgeTextScale);
        currentX += badgeSpacing;
        renderBadge(context, currentX, badgeY1, badgeHeight, badgePadding, mobsText, 0x7a3d3d, badgeTextScale);

        currentX = textX;
        currentX = renderBadge(context, currentX, badgeY2, badgeHeight, badgePadding, deathsText, 0x4a3d5a, badgeTextScale);
        currentX += badgeSpacing;
        renderBadge(context, currentX, badgeY2, badgeHeight, badgePadding, jumpsText, 0x5a4a3d, badgeTextScale);
    }

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

