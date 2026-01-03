package mt.client.service;

import mt.client.api.UselessFactsApiClient;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FactProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final int PREFETCH_THRESHOLD = 2;
    private static final int MAX_QUEUE_SIZE = 10;

    private final UselessFactsApiClient apiClient;
    private final SlideRepository slideRepository;
    private final Queue<Slide> factQueue;
    private final AtomicBoolean apiAvailable;
    private final AtomicBoolean fetchInProgress;

    private String currentLanguage = "en_us";

    public FactProvider(UselessFactsApiClient apiClient, SlideRepository slideRepository) {
        this.apiClient = apiClient;
        this.slideRepository = slideRepository;
        this.factQueue = new ConcurrentLinkedQueue<>();
        this.apiAvailable = new AtomicBoolean(true);
        this.fetchInProgress = new AtomicBoolean(false);
    }

    public Slide getNextFact(String language) {
        if (!language.equals(currentLanguage)) {
            currentLanguage = language;
            factQueue.clear();
        }

        prefetchIfNeeded();

        Slide queuedFact = factQueue.poll();
        if (queuedFact != null) {
            return queuedFact;
        }

        return getFallbackFact(language);
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
                        Slide slide = Slide.of(text, SlideCategory.FACT);
                        if (factQueue.size() < MAX_QUEUE_SIZE) {
                            factQueue.offer(slide);
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