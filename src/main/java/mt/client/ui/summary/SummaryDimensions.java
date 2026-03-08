package mt.client.ui.summary;

public class SummaryDimensions {
    public int panelWidth;
    public int panelHeight;
    public int panelX;
    public int panelY;
    public int playerRowHeight;
    public int headSize;
    public float uiScale;
    public int playersPerPage;

    public void calculate(int screenWidth, int screenHeight) {
        uiScale = calculateUIScale(screenWidth, screenHeight, 854.0f, 480.0f, 0.6f, 1.5f);
        playersPerPage = SummaryConstants.NORMAL_MODE_PLAYERS_PER_PAGE;

        int maxPanelHeight = Math.min(screenHeight - 120, (int)(400 * uiScale));
        panelWidth = (int)(maxPanelHeight * SummaryConstants.FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > screenWidth - 60) {
            panelWidth = screenWidth - 60;
            panelHeight = (int)(panelWidth / SummaryConstants.FRAME_ASPECT_RATIO);
        }

        playerRowHeight = (int)(130 * uiScale);
        headSize = (int)(64 * uiScale);

        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2 - (int)(30 * uiScale);
    }

    private float calculateUIScale(int screenWidth, int screenHeight, float baseWidth, float baseHeight, float minScale, float maxScale) {
        float scale = Math.min((float)screenWidth / baseWidth, (float)screenHeight / baseHeight);
        return Math.max(minScale, Math.min(maxScale, scale));
    }

    public int getTotalPages(int totalPlayers) {
        return Math.max(1, (int) Math.ceil((double) totalPlayers / playersPerPage));
    }

    public int getButtonWidth() {
        return (int)(125 * uiScale);
    }

    public int getButtonHeight() {
        return (int)(28 * uiScale);
    }
}