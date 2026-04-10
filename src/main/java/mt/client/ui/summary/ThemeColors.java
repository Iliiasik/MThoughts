package mt.client.ui.summary;

public class ThemeColors {
    public static ThemeColor getThemeColors(String theme) {
        return switch (theme) {
            case "vanilla" -> new ThemeColor(
                    0xf7d5a3,
                    0x3f3f3f,
                    0x1a1a1a,
                    0x8a5a2a,
                    0xFFFFFF,
                    0xf3f3f3,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "classic" -> new ThemeColor(
                    0x3b1a17,
                    0x3b1a17,
                    0x5a3a1a,
                    0x8a5a2a,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "tech" -> new ThemeColor(
                    0x4a5a5e,
                    0x2a3a3e,
                    0x2a3a3e,
                    0x4a5a5e,
                    0xc8d8e0,
                    0xe0f0f8,
                    0xFFFFFF,
                    0xFFFFFF
            );
            case "magic" -> new ThemeColor(
                    0x68503c,
                    0x3b1a17,
                    0xc8a3d8,
                    0xd8a848,
                    0xddccee,
                    0xeeddff,
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