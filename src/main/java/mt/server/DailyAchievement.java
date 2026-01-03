package mt.server;

public enum DailyAchievement {
    DISTANCE_RECORD("distance_record"),
    BLOCKS_RECORD("blocks_record"),
    MOBS_RECORD("mobs_record"),
    EXPLORER("explorer"),
    MINER_PRO("miner_pro"),
    HUNTER("hunter"),
    MASS_MURDERER("mass_murderer"),
    DEATHLESS("deathless"),
    SURVIVOR("survivor"),
    CLUMSY("clumsy"),
    JUMPER("jumper"),
    MARATHON("marathon"),
    FIRST_BLOOD("first_blood"),
    UNTOUCHABLE("untouchable"),
    WORKAHOLIC("workaholic"),
    NOMAD("nomad"),
    PACIFIST("pacifist"),
    SPEEDRUNNER("speedrunner"),
    LAZY("lazy"),
    HYPERACTIVE("hyperactive"),
    DEMOLITION_EXPERT("demolition_expert"),
    BUNNY_HOP("bunny_hop"),
    IRON_WILL("iron_will"),
    BLOODTHIRSTY("bloodthirsty"),
    CAREFUL("careful"),
    ADVENTURER("adventurer");

    private final String id;

    DailyAchievement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return "midnightthoughts.achievement." + id;
    }
}

