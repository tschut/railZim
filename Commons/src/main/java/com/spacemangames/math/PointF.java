package com.spacemangames.math;

public class PointF {
    public float x;
    public float y;
    
    public PointF() {
        this(0f, 0f);
    }
    
    public PointF(float aX, float aY) {
        set(aX, aY);
    }
    
    public void set(float aX, float aY) {
        x = aX;
        y = aY;
    }
}
