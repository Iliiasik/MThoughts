package mt.client.service;

import mt.client.config.MidnightThoughtsConfig;
import mt.client.model.PlayerSleepStats;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.text.Text;

import java.util.concurrent.ThreadLocalRandom;

public class SlideService {
    private final SlideRepository slideRepository;
    private final PlayerStatsService playerStatsService;
    private final MidnightThoughtsConfig config;
    private final FactProvider factProvider;

    private int slideCounter = 0;
    private PlayerSleepStats cachedStats;

    public SlideService(SlideRepository slideRepository, PlayerStatsService playerStatsService,
                        MidnightThoughtsConfig config, FactProvider factProvider) {
        this.slideRepository = slideRepository;
        this.playerStatsService = playerStatsService;
        this.config = config;
        this.factProvider = factProvider;
    }

    public Slide getNextSlide() {
        slideCounter++;
        String language = getCurrentLanguage();

        if (shouldShowSpecialSlide()) {
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

    private Slide getSlideByCategory(String language, SlideCategory category) {
        if (category == SlideCategory.FACT) {
            return factProvider.getNextFact(language);
        }
        return slideRepository.getRandomSlide(language, category);
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

    private Slide generateStatsSlide() {
        if (cachedStats == null) {
            refreshStats();
        }

        String text = generateStatsText();
        return Slide.of(text, SlideCategory.PLAYER_STATS);
    }

    private String generateStatsText() {
        int statType = ThreadLocalRandom.current().nextInt(10);

        return switch (statType) {
            case 0 -> formatPlayTime();
            case 1 -> formatWalkDistance();
            case 2 -> formatJumps();
            case 3 -> formatMobsKilled();
            case 4 -> formatDeaths();
            case 5 -> formatSleepInBed();
            case 6 -> formatDamageDealt();
            case 7 -> formatBlocksDestroyed();
            case 8 -> formatItemsCrafted();
            default -> formatFishCaught();
        };
    }

    private String formatPlayTime() {
        long minutes = cachedStats.getPlayTimeMinutes();
        long hours = minutes / 60;
        long mins = minutes % 60;
        return Text.translatable("midnightthoughts.stats.playtime", hours, mins).getString();
    }

    private String formatWalkDistance() {
        double km = cachedStats.getTotalWalkDistanceKm();
        String formatted = String.format("%.2f", km);
        return Text.translatable("midnightthoughts.stats.walk_distance", formatted).getString();
    }

    private String formatJumps() {
        return Text.translatable("midnightthoughts.stats.jumps", cachedStats.getJumps()).getString();
    }

    private String formatMobsKilled() {
        return Text.translatable("midnightthoughts.stats.mobs_killed", cachedStats.getMobsKilled()).getString();
    }

    private String formatDeaths() {
        return Text.translatable("midnightthoughts.stats.deaths", cachedStats.getDeaths()).getString();
    }

    private String formatSleepInBed() {
        return Text.translatable("midnightthoughts.stats.sleep_in_bed", cachedStats.getSleepInBed()).getString();
    }

    private String formatDamageDealt() {
        int damage = cachedStats.getDamageDealt() / 10;
        return Text.translatable("midnightthoughts.stats.damage_dealt", damage).getString();
    }

    private String formatBlocksDestroyed() {
        return Text.translatable("midnightthoughts.stats.blocks_destroyed", cachedStats.getBlocksDestroyed()).getString();
    }

    private String formatItemsCrafted() {
        return Text.translatable("midnightthoughts.stats.items_crafted", cachedStats.getItemsCrafted()).getString();
    }

    private String formatFishCaught() {
        return Text.translatable("midnightthoughts.stats.fish_caught", cachedStats.getFishCaught()).getString();
    }

    public void refreshStats() {
        cachedStats = playerStatsService.collectStats();
    }

    public void resetSlideCounter() {
        slideCounter = 0;
    }

    public String getCurrentLanguage() {
        MinecraftClient client = MinecraftClient.getInstance();
        LanguageManager langManager = client.getLanguageManager();
        String lang = langManager.getLanguage();

        if (lang.startsWith("de")) {
            return "de_de";
        }
        return "en_us";
    }
}
