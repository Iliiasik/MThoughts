package mt.client.service;

import mt.config.MidnightThoughtsConfig;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;

import java.util.concurrent.ThreadLocalRandom;

public class SlideService {
    private final SlideRepository slideRepository;
    private final MidnightThoughtsConfig config;
    private final FactProvider factProvider;

    public SlideService(SlideRepository slideRepository,
                        MidnightThoughtsConfig config, FactProvider factProvider) {
        this.slideRepository = slideRepository;
        this.config = config;
        this.factProvider = factProvider;
    }

    public Slide getSlideByCategory(String language, SlideCategory category) {
        if (category == SlideCategory.FACT) {
            return factProvider.getNextFact(language);
        }

        Slide userSlide = factProvider.getNextForCategory(language, category);
        if (userSlide != null) {
            return userSlide;
        }

        return slideRepository.getRandomSlide(language, category);
    }

    public Slide getNextSlide() {
        String language = getCurrentLanguage();

        if (shouldShowSpecialSlide()) {
            Slide userSpecial = factProvider.getNextForCategory(language, SlideCategory.SPECIAL);
            if (userSpecial != null) {
                return userSpecial;
            }
            Slide special = slideRepository.getRandomSlideByRarity(language, SlideCategory.SPECIAL);
            if (special != null) {
                return special;
            }
        }

        SlideCategory category = selectRandomCategory();
        Slide slide = getSlideByCategory(language, category);

        if (slide == null) {
            return slideRepository.getRandomSlide(language, SlideCategory.FACT);
        }

        return slide;
    }

    private boolean shouldShowSpecialSlide() {
        return ThreadLocalRandom.current().nextFloat() < config.getSpecialSlideChance();
    }

    private SlideCategory selectRandomCategory() {
        float random = ThreadLocalRandom.current().nextFloat();

        if (random < 0.4f) {
            return SlideCategory.FACT;
        } else if (random < 0.7f) {
            return SlideCategory.LORE;
        } else {
            return SlideCategory.SURREAL;
        }
    }

    public String getCurrentLanguage() {
        Minecraft client = Minecraft.getInstance();
        LanguageManager langManager = client.getLanguageManager();
        String lang = langManager.getSelected();

        if (lang.startsWith("de")) {
            return "de_de";
        }
        return "en_us";
    }
}