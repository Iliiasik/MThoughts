package mt.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportedLanguagesTest {

    @Test
    void defaultIsEnglish() {
        assertEquals("en_us", SupportedLanguages.DEFAULT);
        assertTrue(SupportedLanguages.codes().contains(SupportedLanguages.DEFAULT));
    }

    @Test
    void codesAreUniqueAndWellFormed() {
        List<String> codes = SupportedLanguages.codes();
        Set<String> unique = new HashSet<>(codes);
        assertEquals(codes.size(), unique.size(), "duplicate language code in SupportedLanguages");
        for (String code : codes) {
            assertTrue(code.matches("[a-z]{2}_[a-z]{2}"), "malformed language code: " + code);
        }
    }

    @Test
    void apiSupportedIsSubsetOfCodes() {
        Set<String> api = SupportedLanguages.apiSupported();
        assertFalse(api.isEmpty());
        assertTrue(SupportedLanguages.codes().containsAll(api));
        assertTrue(api.contains(SupportedLanguages.DEFAULT));
    }

    @Test
    void apiSupportedSetIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> SupportedLanguages.apiSupported().add("xx_xx"));
    }

    @Test
    void codesListIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> SupportedLanguages.codes().add("xx_xx"));
    }

    @Test
    void accessorsReturnStableInstances() {
        assertSame(SupportedLanguages.codes(), SupportedLanguages.codes());
        assertSame(SupportedLanguages.apiSupported(), SupportedLanguages.apiSupported());
    }

    @ParameterizedTest
    @CsvSource({
            "en_us, en_us",
            "de_de, de_de",
            "ru_ru, ru_ru",
            "zh_cn, zh_cn",
            "pt_br, pt_br",
            "uk_ua, uk_ua",
            "EN_US, en_us",
            "De_De, de_de",
            "en_gb, en_us",
            "de_at, de_de",
            "es_mx, es_es",
            "pt_pt, pt_br",
            "zh_tw, zh_cn",
            "fr_ca, fr_fr"
    })
    void fromLocaleResolvesExactThenPrefix(String input, String expected) {
        assertEquals(expected, SupportedLanguages.fromLocale(input));
    }

    @ParameterizedTest
    @CsvSource({
            "nl_nl",
            "sv_se",
            "hu_hu",
            "''",
            "x",
            "not_a_locale",
            "'  '"
    })
    void unknownLocaleFallsBackToDefault(String input) {
        assertEquals(SupportedLanguages.DEFAULT, SupportedLanguages.fromLocale(input));
    }

    @Test
    void nullLocaleFallsBackToDefault() {
        assertEquals(SupportedLanguages.DEFAULT, SupportedLanguages.fromLocale(null));
    }

    @Test
    void everyCodeResolvesToItself() {
        for (String code : SupportedLanguages.codes()) {
            assertEquals(code, SupportedLanguages.fromLocale(code),
                    "code " + code + " does not round-trip through fromLocale");
        }
    }

    @Test
    void resolvedValueIsAlwaysASupportedCode() {
        for (char a = 'a'; a <= 'z'; a++) {
            for (char b = 'a'; b <= 'z'; b++) {
                String locale = "" + a + b + "_" + a + b;
                String resolved = SupportedLanguages.fromLocale(locale);
                assertNotNull(resolved);
                assertTrue(SupportedLanguages.codes().contains(resolved),
                        locale + " resolved to unsupported " + resolved);
            }
        }
    }

    @Test
    void fromLocaleIsThreadSafeUnderLoad() throws Exception {
        int threads = 16;
        int iterations = 20_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger mismatches = new AtomicInteger();
        List<String> codes = SupportedLanguages.codes();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < iterations; i++) {
                        String code = codes.get(i % codes.size());
                        if (!code.equals(SupportedLanguages.fromLocale(code))) {
                            mismatches.incrementAndGet();
                        }
                        if (!codes.contains(SupportedLanguages.fromLocale("qq_qq"))) {
                            mismatches.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "load test did not finish in time");
        assertEquals(0, mismatches.get());
    }

    @Test
    void repeatedResolutionIsCheap() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 200_000; i++) {
                SupportedLanguages.fromLocale("ru_ru");
            }
        });
    }

}
