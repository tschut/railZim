package com.spacemangames.math;

public class RectF {
    public float left;
    public float right;
    public float top;
    public float bottom;

    public RectF() {
        this(0, 0, 0, 0);
    }

    public RectF(float aLeft, float aTop, float aRight, float aBottom) {
        set(aLeft, aTop, aRight, aBottom);
    }

    public void set(float aLeft, float aTop, float aRight, float aBottom) {
        left = aLeft;
        top = aTop;
        right = aRight;
        bottom = aBottom;
    }

    public void set(Rect aOther) {
        set(aOther.left, aOther.top, aOther.right, aOther.bottom);
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
