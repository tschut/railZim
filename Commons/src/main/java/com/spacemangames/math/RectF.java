package com.spacemangames.math;

public class RectF {
    public float left;
    public float right;
    public float top;
    public float bottom;

    public RectF() {
        this(0, 0, 0, 0);
    }

    public RectF(float left, float top, float right, float bottom) {
        set(left, top, right, bottom);
    }

    public void set(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(Rect other) {
        set(other.left, other.top, other.right, other.bottom);
    }

    private float exactCenter(float small, float big) {
        return small + ((big - small) / 2f);
    }

    public float exactCenterX() {
        return exactCenter(left, right);
    }

    public float exactCenterY() {
        return exactCenter(top, bottom);
    }

    public float height() {
        return bottom - top;
    }

    public float width() {
        return right - left;
    }
}
