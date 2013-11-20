package com.spacemangames.framework;

import com.spacemangames.math.PointF;

public interface IMoveProperties {

    public abstract void reset();

    public abstract void elapse(double elapsedMilliseconds);

    public abstract PointF getPos();

}