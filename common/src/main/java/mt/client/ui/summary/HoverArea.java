package mt.client.ui.summary;

import java.util.List;

public record HoverArea(int x, int y, int width, int height, List<String> lines) {

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
