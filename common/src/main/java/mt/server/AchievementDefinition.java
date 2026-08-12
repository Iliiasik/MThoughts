package mt.server;

public class AchievementDefinition {
    public String id;
    public String name;
    public String tooltip;
    public Conditions conditions;

    public static class Conditions {
        public Integer deathsEq;
        public Integer deathsMin;
        public Integer deathsMax;

        public Integer mobsKilledEq;
        public Integer mobsKilledMin;
        public Integer mobsKilledMax;

        public Integer blocksDestroyedEq;
        public Integer blocksDestroyedMin;
        public Integer blocksDestroyedMax;

        public Integer distanceWalkedEq;
        public Integer distanceWalkedMin;
        public Integer distanceWalkedMax;

        public Integer jumpsEq;
        public Integer jumpsMin;
        public Integer jumpsMax;

        public Integer damageDealtEq;
        public Integer damageDealtMin;
        public Integer damageDealtMax;
    }
}
