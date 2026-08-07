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
    private final Queue<Slide> factQueue;
    private final AtomicLong nextApiAttempt;
    private final AtomicBoolean fetchInProgress;

    private String currentLanguage = mt.common.SupportedLanguages.DEFAULT;

    public FactProvider(UselessFactsApiClient apiClient, SlideRepository slideRepository, UserContentLoader userContentLoader) {
        this.apiClient = apiClient;
        this.slideRepository = slideRepository;
        this.userContentLoader = userContentLoader;
        this.factQueue = new ConcurrentLinkedQueue<>();
        this.nextApiAttempt = new AtomicLong(0L);
        this.fetchInProgress = new AtomicBoolean(false);
    }

    public Slide getNextFact(String language) {
        updateLanguageIfChanged(language);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        List<String> userFacts = userContentLoader.loadEntries(language, "facts");

        if (!userFacts.isEmpty() && config.isUserContentReplaces()) {
            return randomUserSlide(userFacts, SlideCategory.FACT);
        }

        if (!userFacts.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
            return randomUserSlide(userFacts, SlideCategory.FACT);
        }

        if (!config.isUseFactsApi() || !API_SUPPORTED_LANGUAGES.contains(language)) {
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

        String categoryName = category.name().toLowerCase();

        if (category == SlideCategory.NIGHTMARE) {
            categoryName = "nightmares";
        }

        List<String> userEntries = userContentLoader.loadEntries(language, categoryName);
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();

        if (!userEntries.isEmpty() && config.isUserContentReplaces()) {
            return randomUserSlide(userEntries, category);
        }

        if (!userEntries.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
            return randomUserSlide(userEntries, category);
        }

        return null;
    }

    private void updateLanguageIfChanged(String language) {
        if (!language.equals(currentLanguage)) {
            currentLanguage = language;
            factQueue.clear();
        }
    }

    private Slide randomUserSlide(List<String> entries, SlideCategory category) {
        String text = entries.get(ThreadLocalRandom.current().nextInt(entries.size()));
        return Slide.of(text, category);
    }

    private void prefetchIfNeeded() {
        if (factQueue.size() >= PREFETCH_THRESHOLD || fetchInProgress.get()) {
            return;
        }
        if (System.currentTimeMillis() < nextApiAttempt.get()) {
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
