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

    public int getJumps() {
        return jumps;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public int getMobsKilled() {
        return mobsKilled;
    }

    public void setMobsKilled(int mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getSleepInBed() {
        return sleepInBed;
    }

    public void setSleepInBed(int sleepInBed) {
        this.sleepInBed = sleepInBed;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public void setFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
    }

    public int getBlocksDestroyed() {
        return blocksDestroyed;
    }

    public void setBlocksDestroyed(int blocksDestroyed) {
        this.blocksDestroyed = blocksDestroyed;
    }

    public int getItemsCrafted() {
        return itemsCrafted;
    }

    public void setItemsCrafted(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }

    public long getPlayTimeMinutes() {
        return playTimeTicks / 1200;
    }

    public double getTotalWalkDistanceKm() {
        return (walkDistanceCm + sprintDistanceCm) / 100000.0;
    }
}
