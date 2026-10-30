package mt.client.ui.summary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummaryLayoutTest {

    private static final int[][] GUI_SPACES = {
            {320, 240}, {426, 240}, {480, 270}, {640, 360}, {683, 384},
            {854, 480}, {960, 540}, {1280, 720}, {1920, 1080}, {2560, 1440}, {3840, 2160}
    };

    private static SummaryLayout at(int width, int height) {
        SummaryLayout layout = new SummaryLayout();
        layout.calculate(width, height);
        return layout;
    }

    @Test
    void texelGridAddsUpExactly() {
        int content = SummaryConstants.INFO_TEXTURE_WIDTH - SummaryLayout.PAD * 2;
        assertEquals(content,
                SummaryConstants.CHIP_TEXTURE_WIDTH + SummaryLayout.GAP
                        + SummaryConstants.STATS_PANEL_TEXTURE_WIDTH,
                "chip column, gap and stats panel must fill the content width");

        int contentHeight = SummaryConstants.INFO_TEXTURE_HEIGHT - SummaryLayout.PAD * 2;
        assertEquals(contentHeight,
                SummaryConstants.BADGE_TEXTURE_HEIGHT + SummaryLayout.GAP
                        + SummaryConstants.STATS_PANEL_TEXTURE_HEIGHT + SummaryLayout.GAP
                        + SummaryConstants.ACHIEVEMENT_TEXTURE_SIZE,
                "badge, panel and achievement row must fill the content height");
    }

    @Test
    void scaleIsAlwaysAWholeNumberAndAtLeastOne() {
        for (int[] space : GUI_SPACES) {
            SummaryLayout layout = at(space[0], space[1]);
            assertTrue(layout.scale >= 1, "scale fell below one at " + space[0] + "x" + space[1]);
            assertEquals(layout.scale, (int) layout.textScale(),
                    "text scale must match the texel scale at " + space[0] + "x" + space[1]);
        }
    }

    @Test
    void everythingStaysOnScreen() {
        for (int[] space : GUI_SPACES) {
            int width = space[0];
            int height = space[1];
            SummaryLayout layout = at(width, height);
            String at = " at " + width + "x" + height;

            assertTrue(layout.frameX >= 0, "frame starts off screen" + at);
            assertTrue(layout.frameY >= 0, "frame starts above the screen" + at);
            assertTrue(layout.frameX + layout.frameWidth() <= width, "frame overflows right" + at);
            assertTrue(layout.controlRowY() + layout.buttonSize() <= height,
                    "control row overflows the bottom" + at);
        }
    }

    @Test
    void frameIsCenteredHorizontally() {
        for (int[] space : GUI_SPACES) {
            SummaryLayout layout = at(space[0], space[1]);
            int left = layout.frameX;
            int right = space[0] - (layout.frameX + layout.frameWidth());
            assertTrue(Math.abs(left - right) <= 1,
                    "frame is off center at " + space[0] + "x" + space[1]);
        }
    }

    @Test
    void panelAndAchievementsStayInsideTheContent() {
        for (int[] space : GUI_SPACES) {
            SummaryLayout layout = at(space[0], space[1]);
            String at = " at " + space[0] + "x" + space[1];

            assertEquals(layout.contentX() + layout.contentWidth(),
                    layout.panelX() + layout.panelWidth(),
                    "stats panel must end flush with the content" + at);
            assertEquals(layout.contentY() + layout.contentHeight(),
                    layout.achievementsY() + layout.achievementSize(),
                    "achievement row must end flush with the content" + at);
            assertTrue(layout.achievementRowWidth(SummaryLayout.ACHIEVEMENT_COUNT) <= layout.panelWidth(),
                    "achievement row is wider than the panel" + at);
        }
    }

    @Test
    void theNightsChipSitsUnderTheHeadAndLeavesRoomToDrawOn() {
        for (int[] space : GUI_SPACES) {
            SummaryLayout layout = at(space[0], space[1]);
            String at = " at " + space[0] + "x" + space[1];

            int chipTop = layout.chipY();
            int chipBottom = chipTop + layout.s(SummaryConstants.CHIP_TEXTURE_HEIGHT);
            int headBottom = layout.columnsY() + layout.s(SummaryLayout.HEAD);
            int columnBottom = layout.columnsY() + layout.columnsHeight();

            assertEquals(headBottom + layout.s(SummaryLayout.CHIP_GAP), chipTop,
                    "the chip must sit one gap below the head" + at);
            assertTrue(chipBottom < columnBottom,
                    "the chip overflows the column" + at);
            assertTrue(columnBottom - chipBottom >= layout.s(28),
                    "less than 28 texels left for the drawn corner" + at);
        }
    }

    @Test
    void achievementRowIsCenteredForEveryCount() {
        SummaryLayout layout = at(1920, 1080);
        for (int count = 1; count <= SummaryLayout.ACHIEVEMENT_COUNT; count++) {
            int row = layout.achievementRowWidth(count);
            int left = (layout.panelWidth() - row) / 2;
            int right = layout.panelWidth() - row - left;
            assertTrue(Math.abs(left - right) <= 1,
                    "row of " + count + " is off center inside the panel");
            assertTrue(row <= layout.panelWidth(), "row of " + count + " is wider than the panel");
        }
        assertEquals(0, layout.achievementRowWidth(0), "an empty row must take no width");
    }

    @Test
    void calculateIsIdempotent() {
        SummaryLayout layout = at(960, 540);
        int scale = layout.scale;
        int x = layout.frameX;
        int y = layout.frameY;

        layout.calculate(960, 540);

        assertEquals(scale, layout.scale);
        assertEquals(x, layout.frameX);
        assertEquals(y, layout.frameY);
    }
}
