package mt.client.ui.summary;

public class SummaryDimensions {
    public static final float BASE_W = 1000.0f;
    public static final float BASE_H = 640.0f;
    public int panelWidth;
    public int panelHeight;
    public int panelX;
    public int panelY;
    public int playerRowHeight;
    public int headSize;
    public float uiScale = 1.0f;

    public void calculate(int screenWidth, int screenHeight) {
        float scaleX = screenWidth / BASE_W;
        float scaleY = screenHeight / BASE_H;
        uiScale = Math.min(scaleX, scaleY);

        int maxPanelHeight = Math.min(screenHeight - s(160), s(533));
        panelWidth = (int) (maxPanelHeight * SummaryConstants.FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > screenWidth - s(80)) {
            panelWidth = screenWidth - s(80);
            panelHeight = (int) (panelWidth / SummaryConstants.FRAME_ASPECT_RATIO);
        }

        playerRowHeight = s(173);
        headSize = s(85);
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2 - s(40);
    }

    public int s(int virtualValue) {
        return Math.round(virtualValue * uiScale);
    }

    public static int getTotalPages(int totalPlayers) {
        return Math.max(1, (int) Math.ceil((double) totalPlayers / SummaryConstants.PLAYERS_PER_PAGE));
    }

    public int getButtonWidth() {
        return s(100);
    }

    public int getButtonHeight() {
        return s(32);
    }
}
