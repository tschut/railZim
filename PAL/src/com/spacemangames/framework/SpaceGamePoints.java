package com.spacemangames.framework;

public class SpaceGamePoints {
    private static final int POINTS_STARTING   = 10000;
    private static final int POINTS_PER_SECOND = 500;

    private int              points;
    private int              bonusPoints;
    private long             totalElapsed;

    public SpaceGamePoints() {
        reset();
    }

    public synchronized void reset() {
        points = POINTS_STARTING;
        bonusPoints = 0;
        totalElapsed = 0;
    }

    public int getCurrentPoints() {
        updatePoints();

        return points;
    }

    public synchronized void elapse(int elapsedMilliseconds) {
        totalElapsed += elapsedMilliseconds;
    }

    private void updatePoints() {
        int pointsDiff = (int) ((totalElapsed / 1000f) * POINTS_PER_SECOND);
        points = (POINTS_STARTING + bonusPoints) - pointsDiff;

        if (points < 0) {
            points = 0;
        }
    }

    public synchronized void bonus(int scoredBonusPoints) {
        bonusPoints += scoredBonusPoints;
    }
}
