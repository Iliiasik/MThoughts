package mt.client.service;

import mt.client.api.UselessFactsApiClient;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import mt.config.MidnightThoughtsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FactProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final int PREFETCH_THRESHOLD = 2;
    private static final int MAX_QUEUE_SIZE = 10;
    private static final long RETRY_COOLDOWN_MS = 120_000L;
    private static final Set<String> API_SUPPORTED_LANGUAGES = mt.common.SupportedLanguages.apiSupported();

    private final UselessFactsApiClient apiClient;
    private final SlideRepository slideRepository;
    private final UserContentLoader userContentLoader;
    private final Queue<Slide> factQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong nextApiAttempt = new AtomicLong(0L);
    private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);

    private String currentLanguage = mt.common.SupportedLanguages.DEFAULT;

    public FactProvider(UselessFactsApiClient apiClient, SlideRepository slideRepository, UserContentLoader userContentLoader) {
        this.apiClient = apiClient;
        this.slideRepository = slideRepository;
        this.userContentLoader = userContentLoader;
    }

    public Slide getNextFact(String language) {
        Slide userFact = getNextForCategory(language, SlideCategory.FACT);
        if (userFact != null) {
            return userFact;
        }

        if (!MidnightThoughtsConfig.getInstance().isUseFactsApi() || !API_SUPPORTED_LANGUAGES.contains(language)) {
            return getFallbackFact(language);
        }

        prefetchIfNeeded();

        Slide queued = factQueue.poll();
        if (queued != null) {
            return queued;
        }

        return getFallbackFact(language);
    }

    public Slide getNextForCategory(String language, SlideCategory category) {
        updateLanguageIfChanged(language);

        List<String> entries = userContentLoader.loadEntries(language, category.fileName());
        if (entries.isEmpty()) return null;
        if (!MidnightThoughtsConfig.getInstance().isUserContentReplaces() && ThreadLocalRandom.current().nextBoolean()) {
            return null;
        }
        String text = entries.get(ThreadLocalRandom.current().nextInt(entries.size()));
        return Slide.of(text, category);
    }

    private void updateLanguageIfChanged(String language) {
        if (!language.equals(currentLanguage)) {
            currentLanguage = language;
            factQueue.clear();
        }
    }

    private void prefetchIfNeeded() {
        if (factQueue.size() >= PREFETCH_THRESHOLD || System.currentTimeMillis() < nextApiAttempt.get()) {
            return;
        }
        fetchFromApi();
    }

    private void fetchFromApi() {
        if (!fetchInProgress.compareAndSet(false, true)) {
            return;
        }

        int fetchCount = Math.max(1, MAX_QUEUE_SIZE - factQueue.size());
        CompletableFuture<?>[] futures = new CompletableFuture[fetchCount];

        for (int i = 0; i < fetchCount; i++) {
            futures[i] = apiClient.fetchRandomFact(currentLanguage)
                    .thenAccept(optionalFact -> optionalFact.ifPresent(text -> {
                        if (factQueue.size() < MAX_QUEUE_SIZE) {
                            factQueue.offer(Slide.of(text, SlideCategory.FACT));
                        }
                    }));
        }

        CompletableFuture.allOf(futures)
                .whenComplete((result, error) -> {
                    fetchInProgress.set(false);
                    if (factQueue.isEmpty()) {
                        long previous = nextApiAttempt.getAndSet(System.currentTimeMillis() + RETRY_COOLDOWN_MS);
                        if (previous == 0L) {
                            LOGGER.info("API unavailable, falling back to local facts and retrying in {} seconds",
                                    RETRY_COOLDOWN_MS / 1000);
                        }
                    } else if (nextApiAttempt.getAndSet(0L) != 0L) {
                        LOGGER.info("API connection restored");
                    }
                });
    }

    private Slide getFallbackFact(String language) {
        Slide localFact = slideRepository.getRandomSlide(language, SlideCategory.FACT);
        if (localFact != null) {
            return localFact;
        }
        return slideRepository.getRandomSlide(mt.common.SupportedLanguages.DEFAULT, SlideCategory.FACT);
    }
}
