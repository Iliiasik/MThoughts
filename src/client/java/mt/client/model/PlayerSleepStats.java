package mt.client.model;

public class PlayerSleepStats {
    private long playTimeTicks;
    private int walkDistanceCm;
    private int sprintDistanceCm;
    private int jumps;
    private int mobsKilled;
    private int deaths;
    private int sleepInBed;
    private int damageDealt;
    private int fishCaught;
    private int blocksDestroyed;
    private int itemsCrafted;

    public void setPlayTimeTicks(long playTimeTicks) {
        this.playTimeTicks = playTimeTicks;
    }

    public void setWalkDistanceCm(int walkDistanceCm) {
        this.walkDistanceCm = walkDistanceCm;
    }

    public void setSprintDistanceCm(int sprintDistanceCm) {
        this.sprintDistanceCm = sprintDistanceCm;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public void setMobsKilled(int mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setSleepInBed(int sleepInBed) {
        this.sleepInBed = sleepInBed;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public void setFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
    }

    public void setBlocksDestroyed(int blocksDestroyed) {
        this.blocksDestroyed = blocksDestroyed;
    }

    public void setItemsCrafted(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }
}
