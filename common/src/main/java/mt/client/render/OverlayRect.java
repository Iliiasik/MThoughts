package mt.client.render;

import net.minecraft.util.Mth;

public record OverlayRect(int x0, int y0, int x1, int y1) {
    public boolean contains(int x, int y, int margin) {
        return x >= x0 - margin && x <= x1 + margin && y >= y0 - margin && y <= y1 + margin;
    }

    public boolean intersects(int cx, int cy, int radius) {
        int nearestX = Mth.clamp(cx, x0, x1);
        int nearestY = Mth.clamp(cy, y0, y1);
        int dx = cx - nearestX;
        int dy = cy - nearestY;
        return dx * dx + dy * dy <= radius * radius;
    }
}
