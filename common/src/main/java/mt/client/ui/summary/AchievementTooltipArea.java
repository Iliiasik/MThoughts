package mt.client.ui.summary;

public record AchievementTooltipArea(int x, int y, int width, int height, String achievementId) {

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
