package com.spacemangames.framework;

import com.spacemangames.math.PointF;

public class MoveProperties {
    public boolean move;
    public int     degreesPerSecond;
    public int     radius;
    public int     offset;

    private double elapsedTime;

    private PointF currentPosition;

    public MoveProperties() {
        move = false;
        currentPosition = new PointF();
    }

    public void reset() {
        elapsedTime = 0.0;
        currentPosition.set(0, 0);
        elapse(0);
    }

    public void elapse(double elapsedMilliseconds) {
        if (!move) {
            return;
        }

        elapsedTime += elapsedMilliseconds;

        double lX = (radius * Math.cos((offset + elapsedTime * degreesPerSecond) * Math.PI / 180.0));
        double lY = (radius * Math.sin((offset + elapsedTime * degreesPerSecond) * Math.PI / 180.0));

        currentPosition.set((float) lX, (float) lY);
    }

    public PointF getPos() {
        return currentPosition;
    }
}
