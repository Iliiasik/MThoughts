package mt.client.service;

import mt.client.api.UselessFactsApiClient;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import mt.config.MidnightThoughtsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class FactProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final int PREFETCH_THRESHOLD = 2;
    private static final int MAX_QUEUE_SIZE = 10;

    private final UselessFactsApiClient apiClient;
    private final SlideRepository slideRepository;
    private final UserContentLoader userContentLoader;
    private final Queue<Slide> factQueue;
    private final AtomicBoolean apiAvailable;
    private final AtomicBoolean fetchInProgress;
    private final Map<String, List<String>> userContentCache = new ConcurrentHashMap<>();

    private String currentLanguage = "en_us";

    public FactProvider(UselessFactsApiClient apiClient, SlideRepository slideRepository, UserContentLoader userContentLoader) {
        this.apiClient = apiClient;
        this.slideRepository = slideRepository;
        this.userContentLoader = userContentLoader;
        this.factQueue = new ConcurrentLinkedQueue<>();
        this.apiAvailable = new AtomicBoolean(true);
        this.fetchInProgress = new AtomicBoolean(false);
    }

    public Slide getNextFact(String language) {
        updateLanguageIfChanged(language);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        List<String> userFacts = getCachedUserContent(language, "facts");

        if (!userFacts.isEmpty() && config.isUserContentReplaces()) {
            return randomUserSlide(userFacts, SlideCategory.FACT);
        }

        if (!userFacts.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
            return randomUserSlide(userFacts, SlideCategory.FACT);
        }

        if (!config.isUseFactsApi()) {
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

        List<String> userEntries = getCachedUserContent(language, categoryName);

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
            userContentCache.clear();
        }
    }

    private List<String> getCachedUserContent(String language, String category) {
        String key = language + ":" + category;
        return userContentCache.computeIfAbsent(key, k -> {
            List<String> entries = userContentLoader.loadEntries(language, category);
            if (!entries.isEmpty()) {
                LOGGER.info("Loaded {} user entries for {}/{}", entries.size(), language, category);
            }
            return entries;
        });
    }

    private Slide randomUserSlide(List<String> entries, SlideCategory category) {
        String text = entries.get(ThreadLocalRandom.current().nextInt(entries.size()));
        return Slide.of(text, category);
    }

    private void prefetchIfNeeded() {
        if (factQueue.size() < PREFETCH_THRESHOLD && !fetchInProgress.get() && apiAvailable.get()) {
            fetchFromApi();
        }
    }

    private void fetchFromApi() {
        if (!fetchInProgress.compareAndSet(false, true)) {
            return;
        }

        int fetchCount = MAX_QUEUE_SIZE - factQueue.size();
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
                        if (apiAvailable.get()) {
                            LOGGER.info("API unavailable, switching to fallback mode");
                            apiAvailable.set(false);
                        }
                    } else {
                        if (!apiAvailable.get()) {
                            LOGGER.info("API connection restored");
                            apiAvailable.set(true);
                        }
                    }
                });
    }

    private Slide getFallbackFact(String language) {
        Slide localFact = slideRepository.getRandomSlide(language, SlideCategory.FACT);
        if (localFact != null) {
            return localFact;
        }
        return slideRepository.getRandomSlide("en_us", SlideCategory.FACT);
    }
}