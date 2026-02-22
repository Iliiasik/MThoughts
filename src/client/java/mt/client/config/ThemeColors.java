package mt.client.config;

public class ThemeColors {

    public static ThemeColor getThemeColors(String theme) {
        return switch (theme) {
            case "classic" -> new ThemeColor(
                    0x3b1a17,
                    0x3b1a17,
                    0x5a3a1a,
                    0x8a5a2a,
                    0x663f24,
                    0xe8d8c8,
                    0xf5ebe0
            );
            case "tech" -> new ThemeColor(
                    0x4a5a5e,
                    0x2a3a3e,
                    0x2a3a3e,
                    0x4a5a5e,
                    0x5a6a6e,
                    0xc8d8e0,
                    0xe0f0f8
            );
            case "magic" -> new ThemeColor(
                    0x68503c,
                    0x3b1a17,
                    0xc8a3d8,
                    0xd8a848,
                    0x6a5a8a,
                    0xddccee,
                    0xeeddff
            );
            default -> new ThemeColor(
                    0x68503c,
                    0x3b1a17,
                    0xc8a3d8,
                    0xd8a848,
                    0x6a5a8a,
                    0xddccee,
                    0xeeddff
            );
        };
    }

    public record ThemeColor(
            int badgeTextColor,
            int pagesHolderTextColor,
            int sleepingHudTitleColor,
            int sleepingHudCountColor,
            int buttonColor,
            int buttonTextColor,
            int buttonTextHoverColor
    ) {}
}