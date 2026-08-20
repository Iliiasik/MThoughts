package mt.client.ui.summary;

public class ThemeColors {
    public static ThemeColor getThemeColors(String theme) {
        return switch (theme) {
            case "vanilla" -> new ThemeColor(
                    0xFFFFFF,
                    0xFFFFFF,
                    0x1a1a1a,
                    0x2a2a2a,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "classic" -> new ThemeColor(
                    0x3b1a17,
                    0xFFFFFF,
                    0x5a3a1a,
                    0x8a5a2a,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "tech" -> new ThemeColor(
                    0x45ab95,
                    0x45ab95,
                    0xFFFFFF,
                    0x93e4a1,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF
            );
            case "magic" -> new ThemeColor(
                    0x3087b9,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xd97d1f,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF
            );
            default -> new ThemeColor(
                    0x1a1a1a,
                    0x1a1a1a,
                    0x5a3a1a,
                    0x8a5a2a,
                    0x1a1a1a,
                    0x000000,
                    0x1a1a1a,
                    0x1a1a1a
            );
        };
    }

    public record ThemeColor(
            int badgeTextColor,
            int pagesHolderTextColor,
            int sleepingHudTitleColor,
            int sleepingHudCountColor,
            int buttonTextColor,
            int buttonTextHoverColor,
            int statTextColor,
            int achievementTextColor
    ) {}
}