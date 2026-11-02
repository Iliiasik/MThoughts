package mt.client.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatter {
    private static final DecimalFormat DECIMAL_FORMAT =
            new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final long BILLION = 1_000_000_000L;

    public static String formatLargeNumber(int number) {
        return formatLargeNumber((long) number);
    }

    public static boolean isAbbreviated(long number) {
        return Math.abs(number) >= THOUSAND;
    }

    public static String formatLargeNumber(long number) {
        if (number < 0) {
            return "-" + formatLargeNumber(-number);
        }

        if (number < THOUSAND) {
            return String.valueOf(number);
        } else if (number < MILLION) {
            double value = number / (double) THOUSAND;
            return DECIMAL_FORMAT.format(value) + "K";
        } else if (number < BILLION) {
            double value = number / (double) MILLION;
            return DECIMAL_FORMAT.format(value) + "M";
        } else {
            double value = number / (double) BILLION;
            return DECIMAL_FORMAT.format(value) + "B";
        }
    }
}
