package mt.client.ui.summary;

public class SummaryDimensions {
    public int panelWidth;
    public int panelHeight;
    public int panelX;
    public int panelY;
    public int playerRowHeight;
    public int headSize;
    public float uiScale;
    public boolean isCompactMode;
    public int playersPerPage;

    public void calculate(int screenWidth, int screenHeight) {
        isCompactMode = screenHeight < SummaryConstants.COMPACT_MODE_HEIGHT_THRESHOLD ||
                        screenWidth < SummaryConstants.COMPACT_MODE_WIDTH_THRESHOLD;

        if (isCompactMode) {
            applyCompactMode(screenWidth, screenHeight);
        } else {
            applyNormalMode(screenWidth, screenHeight);
        }

        calculatePanelPosition(screenWidth, screenHeight);
    }

    private void applyCompactMode(int screenWidth, int screenHeight) {
        uiScale = calculateUIScale(screenWidth, screenHeight, 420.0f, 300.0f, 0.35f, 0.8f);
        playersPerPage = SummaryConstants.COMPACT_MODE_PLAYERS_PER_PAGE;

        int maxPanelHeight = Math.min(screenHeight - 100, (int)(240 * uiScale));
        panelWidth = (int)(maxPanelHeight * SummaryConstants.FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > screenWidth - 40) {
            panelWidth = screenWidth - 40;
            panelHeight = (int)(panelWidth / SummaryConstants.FRAME_ASPECT_RATIO);
        }

        playerRowHeight = (int)(42 * uiScale);
        headSize = (int)(16 * uiScale);
    }

    private void applyNormalMode(int screenWidth, int screenHeight) {
        uiScale = calculateUIScale(screenWidth, screenHeight, 854.0f, 480.0f, 0.6f, 1.5f);
        playersPerPage = SummaryConstants.NORMAL_MODE_PLAYERS_PER_PAGE;

        int maxPanelHeight = Math.min(screenHeight - 120, (int)(400 * uiScale));
        panelWidth = (int)(maxPanelHeight * SummaryConstants.FRAME_ASPECT_RATIO);
        panelHeight = maxPanelHeight;

        if (panelWidth > screenWidth - 60) {
            panelWidth = screenWidth - 60;
            panelHeight = (int)(panelWidth / SummaryConstants.FRAME_ASPECT_RATIO);
        }

        playerRowHeight = (int)(65 * uiScale);
        headSize = (int)(32 * uiScale);
    }

    private float calculateUIScale(int screenWidth, int screenHeight, float baseWidth, float baseHeight, float minScale, float maxScale) {
        float scale = Math.min((float)screenWidth / baseWidth, (float)screenHeight / baseHeight);
        return Math.max(minScale, Math.min(maxScale, scale));
    }

    private void calculatePanelPosition(int screenWidth, int screenHeight) {
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2 - (int)(30 * uiScale);
    }

    public int getTotalPages(int totalPlayers) {
        return Math.max(1, (int) Math.ceil((double) totalPlayers / playersPerPage));
    }

    public int getButtonWidth() {
        return isCompactMode ? Math.max(60, (int)(70 * uiScale)) : (int)(90 * uiScale);
    }

    public int getButtonHeight() {
        return isCompactMode ? Math.max(16, (int)(16 * uiScale)) : (int)(20 * uiScale);
    }
}