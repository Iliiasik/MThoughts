package mt.client.ui.summary;

public class AnimationHelper {

    public static float getProgress(long animationStartTime) {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed < 0) return 0.0f;
        float progress = Math.min(1.0f, elapsed / (float) SummaryConstants.STAT_ANIMATION_DURATION);
        return easeOutQuad(progress);
    }

    private static float easeOutQuad(float t) {
        return t * (2 - t);
    }
}
