package mt.client.ui.summary;

import java.util.List;
import java.util.function.Supplier;

public record HoverArea(int x, int y, int width, int height, Supplier<List<String>> lines) {

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
