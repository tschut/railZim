package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;

public class MoveProperties {
    public boolean  mMove;
    public int      mDPS;
    public int      mRadius;
    public int      mOffset;

    private double  mElapsedTime;

    private Vector2 mXY;

    public MoveProperties() {
        mMove = false;
        mXY = new Vector2(0, 0);
    }

    public MoveProperties(boolean aMove, int aDPS, int aRadius, int aOffset) {
        this();

        mMove = aMove;
        mDPS = aDPS;
        mRadius = aRadius;
        mOffset = aOffset;

        reset();
    }

    public void reset() {
        mElapsedTime = 0.0;
        mXY.set(0, 0);
        elapse(0);
    }

    public void elapse(double aTime) {
        if (!mMove) {
            return;
        }

        mElapsedTime += aTime;

        double lX = (mRadius * Math.cos((mOffset + mElapsedTime * mDPS) * Math.PI / 180.0));
        double lY = (mRadius * Math.sin((mOffset + mElapsedTime * mDPS) * Math.PI / 180.0));

        mXY.set((float) lX, (float) lY);
    }

    public Vector2 getPos() {
        return mXY;
    }
}
