package com.spacemangames.framework;

import com.spacemangames.math.PointF;

public class CircularMoveProperties implements IMoveProperties {
    private boolean move;
    private int     degreesPerSecond;
    private int     radius;
    private int     offset;

    private double  elapsedTime;

    private PointF  currentPosition;

    public CircularMoveProperties() {
        move = false;
        currentPosition = new PointF();
    }

    @Override
    public void reset() {
        elapsedTime = 0.0;
        currentPosition.set(0, 0);
        elapse(0);
    }

    @Override
    public void elapse(double elapsedMilliseconds) {
        if (!move) {
            return;
        }

        elapsedTime += elapsedMilliseconds;

        double angle = (offset + elapsedTime * degreesPerSecond) * Math.PI / 180.0;
        currentPosition.set((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
    }

    @Override
    public PointF getPos() {
        return currentPosition;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public void setDegreesPerSecond(int degreesPerSecond) {
        this.degreesPerSecond = degreesPerSecond;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
