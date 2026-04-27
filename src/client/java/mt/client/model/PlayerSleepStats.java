package mt.client.model;

public record PlayerSleepStats(
        long playTimeTicks,
        int walkDistanceCm,
        int sprintDistanceCm,
        int jumps,
        int mobsKilled,
        int deaths,
        int sleepInBed,
        int damageDealt,
        int fishCaught,
        int blocksDestroyed,
        int itemsCrafted
) {
    public static void empty() {
        new PlayerSleepStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}