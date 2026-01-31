package mt.client.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mt.client.MidnightThoughtsClient;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.model.SlideCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SlideRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, Map<SlideCategory, List<Slide>>> slideCache = new HashMap<>();

    public void loadAllSlides(ResourceManager manager) {
        loadSlidesForLanguage(manager, "en_us");
        loadSlidesForLanguage(manager, "de_de");
    }

    private void loadSlidesForLanguage(ResourceManager manager, String language) {
        Map<SlideCategory, List<Slide>> categoryMap = new EnumMap<>(SlideCategory.class);

        categoryMap.put(SlideCategory.FACT, loadCategory(manager, language, "facts", SlideCategory.FACT));
        categoryMap.put(SlideCategory.LORE, loadCategory(manager, language, "lore", SlideCategory.LORE));
        categoryMap.put(SlideCategory.SURREAL, loadCategory(manager, language, "surreal", SlideCategory.SURREAL));
        categoryMap.put(SlideCategory.SPECIAL, loadCategory(manager, language, "special", SlideCategory.SPECIAL));

        slideCache.put(language, categoryMap);

        int total = categoryMap.values().stream().mapToInt(List::size).sum();
        LOGGER.info("Loaded {} slides for language: {}", total, language);
    }

    private List<Slide> loadCategory(ResourceManager manager, String language, String fileName, SlideCategory category) {
        List<Slide> slides = new ArrayList<>();
        ResourceLocation resourceId = new ResourceLocation(MidnightThoughtsClient.MOD_ID, "dreams/" + language + "/" + fileName + ".json");

        try {
            Optional<Resource> resourceOpt = manager.getResource(resourceId);
            if (resourceOpt.isEmpty()) {
                LOGGER.debug("Resource not found: {}", resourceId);
                return slides;
            }
            try (InputStreamReader reader = new InputStreamReader(resourceOpt.get().open(), StandardCharsets.UTF_8)) {
                SlideCollection collection = GSON.fromJson(reader, SlideCollection.class);
                if (collection != null && collection.entries() != null) {
                    for (SlideCollection.SlideEntry entry : collection.entries()) {
                        slides.add(Slide.ofRare(entry.text(), category, entry.rarity()));
                    }
                }
                LOGGER.debug("Loaded {} slides from {}", slides.size(), resourceId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load slides from {}: {}", resourceId, e.getMessage());
        }
        return slides;
    }

    public List<Slide> getSlidesByCategory(String language, SlideCategory category) {
        Map<SlideCategory, List<Slide>> categoryMap = slideCache.get(language);
        if (categoryMap == null) {
            categoryMap = slideCache.get("en_us");
        }
        if (categoryMap == null) {
            return List.of();
        }
        List<Slide> slides = categoryMap.get(category);
        return slides != null ? slides : List.of();
    }

    public Slide getRandomSlide(String language, SlideCategory category) {
        List<Slide> slides = getSlidesByCategory(language, category);
        if (slides.isEmpty()) {
            return null;
        }
        return slides.get(ThreadLocalRandom.current().nextInt(slides.size()));
    }

    public Slide getRandomSlideByRarity(String language, SlideCategory category) {
        List<Slide> slides = getSlidesByCategory(language, category);
        if (slides.isEmpty()) {
            return null;
        }
        float totalWeight = 0;
        for (Slide slide : slides) {
            totalWeight += slide.rarity();
        }
        float random = ThreadLocalRandom.current().nextFloat() * totalWeight;
        float currentWeight = 0;
        for (Slide slide : slides) {
            currentWeight += slide.rarity();
            if (random <= currentWeight) {
                return slide;
            }
        }
        return slides.get(slides.size() - 1);
    }

    public void clearCache() {
        slideCache.clear();
    }
}