package com.spacemangames.math;

public class PointF {
    public float x;
    public float y;

    public PointF() {
        this(0f, 0f);
    }

    public PointF(PointF other) {
        this(other.x, other.y);
    }

    public PointF(float x, float y) {
        set(x, y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void multiply(float factor) {
        x *= factor;
        y *= factor;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public void add(PointF pointF) {
        x += pointF.x;
        y += pointF.y;
    }

    public void subtract(PointF pointF) {
        x -= pointF.x;
        y -= pointF.y;
    }

    public void set(PointF other) {
        set(other.x, other.y);
    }

    public double distanceTo(PointF point2) {
        PointF temp = new PointF(x, y);
        temp.subtract(point2);
        return temp.length();
    }
}
