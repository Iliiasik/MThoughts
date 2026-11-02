package mt.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

public final class Plurals {

    private static final String ONE = "one";
    private static final String FEW = "few";
    private static final String MANY = "many";
    private static final String OTHER = "other";

    private Plurals() {}

    public static String unit(String unitKey, int value) {
        int n = Math.abs(value);
        String key = unitKey + "." + form(Minecraft.getInstance().getLanguageManager().getSelected(), n);
        Language language = Language.getInstance();
        if (!language.has(key)) key = unitKey + "." + OTHER;
        if (!language.has(key)) key = unitKey + "." + ONE;
        return Component.translatable(key).getString();
    }

    private static String form(String language, int n) {
        if (NumberFormatter.isAbbreviated(n)) {
            return MANY;
        }
        if (language.startsWith("ru") || language.startsWith("uk") || language.startsWith("be")) {
            return eastSlavic(n);
        }
        if (language.startsWith("pl")) {
            return polish(n);
        }
        if (language.startsWith("ja") || language.startsWith("ko")
                || language.startsWith("zh") || language.startsWith("tr")) {
            return OTHER;
        }
        if (language.startsWith("fr")) {
            return n <= 1 ? ONE : OTHER;
        }
        return n == 1 ? ONE : OTHER;
    }

    private static String eastSlavic(int n) {
        int mod10 = n % 10;
        int mod100 = n % 100;
        if (mod10 == 1 && mod100 != 11) return ONE;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return FEW;
        return MANY;
    }

    private static String polish(int n) {
        if (n == 1) return ONE;
        int mod10 = n % 10;
        int mod100 = n % 100;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return FEW;
        return MANY;
    }
}
