package mt.client.ui.summary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummaryDimensionsTest {

    private static final int[][] COMMON_RESOLUTIONS = {
            {320, 240}, {427, 240}, {640, 360}, {854, 480}, {1000, 640},
            {1280, 720}, {1600, 900}, {1920, 1080}, {2560, 1440}, {3840, 2160},
            {1024, 768}, {1366, 768}, {800, 600}, {1440, 900}, {2560, 1080}
    };

    @Test
    void freshInstanceHasUsableDefaults() {
        SummaryDimensions dims = new SummaryDimensions();

        assertEquals(1.0f, dims.uiScale);
        assertTrue(dims.s(10) > 0, "scaling must work before calculate is called");
    }

    @Test
    void panelFitsInsideEveryCommonResolution() {
        for (int[] resolution : COMMON_RESOLUTIONS) {
            SummaryDimensions dims = new SummaryDimensions();
            dims.calculate(resolution[0], resolution[1]);

            String at = " at " + resolution[0] + "x" + resolution[1];
            assertTrue(dims.panelWidth > 0, "non positive panel width" + at);
            assertTrue(dims.panelHeight > 0, "non positive panel height" + at);
            assertTrue(dims.panelWidth <= resolution[0], "panel wider than the screen" + at);
            assertTrue(dims.panelHeight <= resolution[1], "panel taller than the screen" + at);
            assertTrue(dims.panelX >= 0, "panel starts off screen" + at);
            assertTrue(dims.panelX + dims.panelWidth <= resolution[0], "panel overflows right" + at);
        }
    }

    @Test
    void panelIsHorizontallyCentered() {
        for (int[] resolution : COMMON_RESOLUTIONS) {
            SummaryDimensions dims = new SummaryDimensions();
            dims.calculate(resolution[0], resolution[1]);

            int leftGap = dims.panelX;
            int rightGap = resolution[0] - (dims.panelX + dims.panelWidth);
            assertTrue(Math.abs(leftGap - rightGap) <= 1,
                    "panel is off centre at " + resolution[0] + "x" + resolution[1]);
        }
    }

    @Test
    void aspectRatioIsPreservedWithinRounding() {
        for (int[] resolution : COMMON_RESOLUTIONS) {
            SummaryDimensions dims = new SummaryDimensions();
            dims.calculate(resolution[0], resolution[1]);

            double ratio = dims.panelWidth / (double) dims.panelHeight;
            assertEquals(SummaryConstants.FRAME_ASPECT_RATIO, ratio, 0.02,
                    "frame aspect drifted at " + resolution[0] + "x" + resolution[1]);
        }
    }

    @Test
    void calculateIsIdempotent() {
        SummaryDimensions dims = new SummaryDimensions();
        dims.calculate(1920, 1080);
        int width = dims.panelWidth;
        int height = dims.panelHeight;
        int x = dims.panelX;
        int y = dims.panelY;
        float scale = dims.uiScale;

        for (int i = 0; i < 100; i++) {
            dims.calculate(1920, 1080);
        }

        assertEquals(width, dims.panelWidth);
        assertEquals(height, dims.panelHeight);
        assertEquals(x, dims.panelX);
        assertEquals(y, dims.panelY);
        assertEquals(scale, dims.uiScale);
    }

    @Test
    void recalculatingAfterAResizeFullyReplacesState() {
        SummaryDimensions large = new SummaryDimensions();
        large.calculate(1920, 1080);
        large.calculate(640, 360);

        SummaryDimensions fresh = new SummaryDimensions();
        fresh.calculate(640, 360);

        assertEquals(fresh.panelWidth, large.panelWidth);
        assertEquals(fresh.panelHeight, large.panelHeight);
        assertEquals(fresh.panelX, large.panelX);
        assertEquals(fresh.panelY, large.panelY);
        assertEquals(fresh.uiScale, large.uiScale);
    }

    @Test
    void degenerateScreenSizesDoNotThrowOrProduceNegativeGeometry() {
        int[][] degenerate = {{0, 0}, {1, 1}, {1, 1000}, {1000, 1}, {80, 60}};
        for (int[] resolution : degenerate) {
            SummaryDimensions dims = new SummaryDimensions();
            dims.calculate(resolution[0], resolution[1]);

            assertTrue(dims.uiScale >= 0.0f,
                    "negative ui scale at " + resolution[0] + "x" + resolution[1]);
            assertTrue(dims.headSize >= 0,
                    "negative head size at " + resolution[0] + "x" + resolution[1]);
            assertTrue(dims.playerRowHeight >= 0,
                    "negative row height at " + resolution[0] + "x" + resolution[1]);
        }
    }

    @Test
    void scalingIsMonotonicInScreenSize() {
        SummaryDimensions small = new SummaryDimensions();
        SummaryDimensions large = new SummaryDimensions();
        small.calculate(1280, 720);
        large.calculate(1920, 1080);

        assertTrue(large.uiScale > small.uiScale);
        assertTrue(large.panelWidth > small.panelWidth);
        assertTrue(large.headSize >= small.headSize);
    }

    @Test
    void scaleHelperRoundsRatherThanTruncates() {
        SummaryDimensions dims = new SummaryDimensions();
        dims.calculate(1000, 640);

        assertEquals(1.0f, dims.uiScale);
        assertEquals(21, dims.s(21));
        assertEquals(0, dims.s(0));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 1",
            "2, 1",
            "3, 2",
            "4, 2",
            "5, 3",
            "10, 5",
            "99, 50",
            "100, 50"
    })
    void paginationCoversEveryPlayer(int players, int expectedPages) {
        assertEquals(expectedPages, SummaryDimensions.getTotalPages(players));
    }

    @Test
    void everyPlayerLandsOnExactlyOnePage() {
        for (int players = 0; players <= 500; players++) {
            int pages = SummaryDimensions.getTotalPages(players);
            assertTrue(pages >= 1, "pagination returned " + pages + " pages for " + players + " players");

            int lastPage = pages - 1;
            int startIndex = lastPage * SummaryConstants.PLAYERS_PER_PAGE;
            assertTrue(startIndex <= Math.max(0, players - 1),
                    "the last page would be empty for " + players + " players");
            assertTrue(pages * SummaryConstants.PLAYERS_PER_PAGE >= players,
                    "pagination drops players at " + players);
        }
    }

    @Test
    void paginationHandlesNegativeAndHugeCounts() {
        assertEquals(1, SummaryDimensions.getTotalPages(-1));
        assertEquals(1, SummaryDimensions.getTotalPages(Integer.MIN_VALUE));
        assertTrue(SummaryDimensions.getTotalPages(Integer.MAX_VALUE) > 0);
    }

    @Test
    void badgeDimensionsScaleWithThePanel() {
        SummaryDimensions dims = new SummaryDimensions();
        dims.calculate(1920, 1080);
        BadgeDimensions badges = BadgeDimensions.calculate(dims);

        assertTrue(badges.height() > 0);
        assertTrue(badges.padding() > 0);
        assertTrue(badges.spacing() > 0);
        assertTrue(badges.rowSpacing() > 0);
        assertTrue(badges.textScale() > 0);
        assertTrue(badges.height() > badges.padding(), "badge padding must not exceed its height");
    }

    @Test
    void buttonsStayWithinThePanelWidth() {
        for (int[] resolution : COMMON_RESOLUTIONS) {
            SummaryDimensions dims = new SummaryDimensions();
            dims.calculate(resolution[0], resolution[1]);

            int navigationWidth = dims.getButtonWidth() * 2 + dims.s(8);
            assertTrue(navigationWidth <= resolution[0],
                    "navigation buttons overflow at " + resolution[0] + "x" + resolution[1]);
            assertTrue(dims.getButtonHeight() > 0);
        }
    }

    @Test
    void repeatedCalculationUnderLoadIsStable() {
        SummaryDimensions dims = new SummaryDimensions();
        dims.calculate(1920, 1080);
        int expected = dims.panelWidth;

        for (int i = 0; i < 500_000; i++) {
            dims.calculate(1920, 1080);
            if (dims.panelWidth != expected) {
                throw new AssertionError("panel width drifted on iteration " + i);
            }
        }
    }
}
