package mt.common;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SupportedLanguages {
    public static final String DEFAULT = "en_us";

    public record Entry(String code, String localePrefix, boolean apiSupported) {}

    private static final List<Entry> ENTRIES = List.of(
            new Entry("en_us", "en", true),
            new Entry("de_de", "de", true),
            new Entry("es_es", "es", false),
            new Entry("ru_ru", "ru", false),
            new Entry("zh_cn", "zh", false),
            new Entry("pt_br", "pt", false),
            new Entry("fr_fr", "fr", false),
            new Entry("uk_ua", "uk", false),
            new Entry("pl_pl", "pl", false),
            new Entry("ja_jp", "ja", false),
            new Entry("ko_kr", "ko", false),
            new Entry("it_it", "it", false),
            new Entry("tr_tr", "tr", false)
    );

    private static final List<String> CODES = ENTRIES.stream().map(Entry::code).toList();

    private static final Set<String> API_SUPPORTED = ENTRIES.stream()
            .filter(Entry::apiSupported)
            .map(Entry::code)
            .collect(Collectors.toUnmodifiableSet());

    private SupportedLanguages() {}

    public static List<String> codes() {
        return CODES;
    }

    public static Set<String> apiSupported() {
        return API_SUPPORTED;
    }

    public static String fromLocale(String locale) {
        if (locale == null) return DEFAULT;
        String normalized = locale.toLowerCase();
        for (Entry entry : ENTRIES) {
            if (normalized.equals(entry.code())) return entry.code();
        }
        for (Entry entry : ENTRIES) {
            if (normalized.startsWith(entry.localePrefix())) return entry.code();
        }
        return DEFAULT;
    }
}
