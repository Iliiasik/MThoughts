package mt.client.ui.summary;

import mt.cache.ServerConfigCache;
import mt.config.MidnightThoughtsConfig;
import mt.config.MidnightThoughtsConfigValidation;
import mt.network.packet.SyncConfigPacket;

public class ThemeColors {

    public static String currentTheme() {
        SyncConfigPacket cached = ServerConfigCache.get();
        if (cached != null && MidnightThoughtsConfigValidation.isValidTheme(cached.theme())) {
            return cached.theme();
        }
        return MidnightThoughtsConfig.getInstance().getUi().theme;
    }

    public static ThemeColor current() {
        return getThemeColors(currentTheme());
    }

    public static ThemeColor getThemeColors(String theme) {
        return switch (theme) {
            case "vanilla" -> new ThemeColor(
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF)
            );
            case "classic" -> new ThemeColor(
                    TextStyle.shadowed(0xF2E6CE),
                    TextStyle.shadowed(0xF2E6CE),
                    TextStyle.shadowed(0xF2E6CE),
                    TextStyle.shadowed(0xFFFFFF)
            );
            case "tech" -> new ThemeColor(
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF)
            );
            case "magic" -> new ThemeColor(
                    TextStyle.flat(0xFFFFFF),
                    TextStyle.flat(0xFFFFFF),
                    TextStyle.shadowed(0xFFFFFF),
                    TextStyle.flat(0xFFFFFF)
            );
            default -> new ThemeColor(
                    TextStyle.flat(0x1a1a1a),
                    TextStyle.flat(0x1a1a1a),
                    TextStyle.flat(0x1a1a1a),
                    TextStyle.flat(0x5a3a1a)
            );
        };
    }

    public record TextStyle(int color, boolean shadow) {
        private static final int MIN_FONT_ALPHA = 4;

        public static TextStyle shadowed(int color) {
            return new TextStyle(color, true);
        }

        public static TextStyle flat(int color) {
            return new TextStyle(color, false);
        }

        public int argb(float alpha) {
            return (Math.max(MIN_FONT_ALPHA, (int) (alpha * 255)) << 24) | color;
        }
    }

    public record ThemeColor(
            TextStyle badge,
            TextStyle days,
            TextStyle tooltip,
            TextStyle sleepingHud
    ) {}
}
