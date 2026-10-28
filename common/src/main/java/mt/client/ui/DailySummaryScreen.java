package mt.client.ui;

import mt.client.ui.summary.*;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DailySummaryScreen extends Screen {
    private static final long FADE_DURATION = 300;

    private final List<DailySummaryPacket.PlayerDailySummary> cards;
    private final List<HoverArea> hoverAreas = new ArrayList<>();
    private final SummaryLayout layout = new SummaryLayout();
    private final long screenOpenTime;
    private final String selfName;

    private int currentPage = 0;
    private float fadeAlpha = 0.0f;

    public DailySummaryScreen(List<DailySummaryPacket.PlayerDailySummary> players) {
        super(Component.literal("Summary"));
        this.selfName = resolveSelfName();
        this.cards = new ArrayList<>(players);
        this.cards.sort(Comparator.comparingInt(this::rank));
        this.screenOpenTime = System.currentTimeMillis();
    }

    private static String resolveSelfName() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getGameProfile().getName() : null;
    }

    private int rank(DailySummaryPacket.PlayerDailySummary card) {
        if (card.isMvp()) return 0;
        if (card.playerName() != null && card.playerName().equals(selfName)) return 1;
        return 2;
    }

    @Override
    protected void init() {
        super.init();
        layout.calculate(width, height);

        if (cards.size() > 1) {
            addNavigationButtons();
        }
    }

    private void addNavigationButtons() {
        int size = layout.buttonSize();
        int gap = layout.s(SummaryLayout.BUTTON_GAP);
        int totalWidth = size * 2 + gap;
        int startX = layout.frameX + (layout.frameWidth() - totalWidth) / 2;
        int y = layout.controlRowY();

        addRenderableWidget(new ArrowButton(startX, y, size, false, button -> turnPage(-1)));
        addRenderableWidget(new ArrowButton(startX + size + gap, y, size, true, button -> turnPage(1)));
    }

    private void turnPage(int delta) {
        int next = currentPage + delta;
        if (next < 0 || next >= cards.size()) return;
        currentPage = next;
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        updateFadeAnimation();
        hoverAreas.clear();

        layout.calculate(width, height);

        if (!cards.isEmpty()) {
            DailySummaryPacket.PlayerDailySummary card = cards.get(Math.min(currentPage, cards.size() - 1));
            FrameRenderer.render(context, layout, card.isMvp(), fadeAlpha);
            FrameRenderer.renderBadge(context, font, layout, headline(card), fadeAlpha);
            CardRenderer.render(context, font, card, layout, fadeAlpha, hoverAreas);
        }

        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget) widget.setAlpha(fadeAlpha);
        }

        super.render(context, mouseX, mouseY, delta);

        TooltipRenderer.render(context, font, mouseX, mouseY, hoverAreas, layout, width, height, fadeAlpha);

    }

    private Component headline(DailySummaryPacket.PlayerDailySummary card) {
        boolean self = card.playerName() != null && card.playerName().equals(selfName);
        if (self) {
            return Component.translatable(card.isMvp()
                    ? "midnightthoughts.summary.headline.self_mvp"
                    : "midnightthoughts.summary.headline.self");
        }
        return Component.translatable(card.isMvp()
                ? "midnightthoughts.summary.headline.friend_mvp"
                : "midnightthoughts.summary.headline.friend", card.playerName());
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    private void updateFadeAnimation() {
        long elapsed = System.currentTimeMillis() - screenOpenTime;
        fadeAlpha = Math.min(1.0f, elapsed / (float) FADE_DURATION);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null) {
            if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                onClose();
                return true;
            }
            if (this.minecraft.options.keyLeft.matches(keyCode, scanCode)) {
                turnPage(-1);
                return true;
            }
            if (this.minecraft.options.keyRight.matches(keyCode, scanCode)) {
                turnPage(1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
