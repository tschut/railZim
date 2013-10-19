package com.spacemangames.math;

public class PointF {
    public float x;
    public float y;

    public PointF() {
        this(0f, 0f);
    }

    public PointF(float x, float y) {
        set(x, y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void mul(float factor) {
        x *= factor;
        y *= factor;
    }
}
