package mt.client.ui.summary;

public final class SummaryLayout {

    public static final int INFO_INSET = 20;
    public static final int PAD = 6;
    public static final int GAP = 6;
    public static final int CHIP_GAP = 4;
    public static final int HEAD = 64;
    public static final int ACHIEVEMENT_GAP = 10;
    public static final int ACHIEVEMENT_COUNT = 3;
    public static final int BUTTON_GAP = 2;
    public static final int SCREEN_MARGIN = 20;

    public static final int CONTROL_ROW = SummaryConstants.BUTTON_TEXTURE_SIZE;

    private static final int TOTAL_HEIGHT =
            SummaryConstants.FRAME_TEXTURE_HEIGHT + BUTTON_GAP + CONTROL_ROW;

    public int scale = 1;
    public int frameX;
    public int frameY;

    public void calculate(int screenWidth, int screenHeight) {
        int k = 1;
        while (SummaryConstants.FRAME_TEXTURE_WIDTH * (k + 1) <= screenWidth - SCREEN_MARGIN
                && TOTAL_HEIGHT * (k + 1) <= screenHeight) {
            k++;
        }
        scale = k;
        frameX = (screenWidth - s(SummaryConstants.FRAME_TEXTURE_WIDTH)) / 2;
        frameY = (screenHeight - s(TOTAL_HEIGHT)) / 2;
    }

    public int s(int texels) {
        return texels * scale;
    }

    public float textScale() {
        return scale;
    }

    public int frameWidth() {
        return s(SummaryConstants.FRAME_TEXTURE_WIDTH);
    }

    public int frameHeight() {
        return s(SummaryConstants.FRAME_TEXTURE_HEIGHT);
    }

    public int infoX() {
        return frameX + s(INFO_INSET);
    }

    public int infoY() {
        return frameY + s(INFO_INSET);
    }

    public int contentX() {
        return infoX() + s(PAD);
    }

    public int contentY() {
        return infoY() + s(PAD);
    }

    public int contentWidth() {
        return s(SummaryConstants.INFO_TEXTURE_WIDTH - PAD * 2);
    }

    public int contentHeight() {
        return s(SummaryConstants.INFO_TEXTURE_HEIGHT - PAD * 2);
    }

    public int columnsY() {
        return contentY() + s(SummaryConstants.BADGE_TEXTURE_HEIGHT + GAP);
    }

    public int columnsHeight() {
        return contentHeight() - s(SummaryConstants.BADGE_TEXTURE_HEIGHT + GAP);
    }

    public int chipColumnWidth() {
        return s(SummaryConstants.CHIP_TEXTURE_WIDTH);
    }

    public int panelX() {
        return contentX() + chipColumnWidth() + s(GAP);
    }

    public int panelWidth() {
        return s(SummaryConstants.STATS_PANEL_TEXTURE_WIDTH);
    }

    public int panelHeight() {
        return s(SummaryConstants.STATS_PANEL_TEXTURE_HEIGHT);
    }

    public int achievementsY() {
        return columnsY() + panelHeight() + s(GAP);
    }

    public int achievementSize() {
        return s(SummaryConstants.ACHIEVEMENT_TEXTURE_SIZE);
    }

    public int achievementRowWidth(int count) {
        if (count <= 0) return 0;
        return count * achievementSize() + (count - 1) * s(ACHIEVEMENT_GAP);
    }

    public int chipY() {
        return columnsY() + s(HEAD + CHIP_GAP);
    }

    public int controlRowY() {
        return frameY + frameHeight() + s(BUTTON_GAP);
    }

    public int buttonSize() {
        return s(SummaryConstants.BUTTON_TEXTURE_SIZE);
    }
}
