package mt.client.config;

public class ThemeColors {
    public static ThemeColor getThemeColors(String theme) {
        return switch (theme) {
            case "vanilla" -> new ThemeColor(
                    0xf7d5a3,
                    0xFFFFFF,
                    0x1a1a1a,
                    0x8a5a2a,
                    0x3f3f3f,
                    0x3f3f3f,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "classic" -> new ThemeColor(
                    0x3b1a17,
                    0xFFFFFF,
                    0x5a3a1a,
                    0x8a5a2a,
                    0x4b371b,
                    0x4b371b,
                    0xFFFFFF,
                    0xf7d5a3
            );
            case "tech" -> new ThemeColor(
                    0x4a5a5e,
                    0xFFFFFF,
                    0x2a3a3e,
                    0x4a5a5e,
                    0x2a3a3e,
                    0x2a3a3e,
                    0xFFFFFF,
                    0xFFFFFF
            );
            case "magic" -> new ThemeColor(
                    0x68503c,
                    0xFFFFFF,
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