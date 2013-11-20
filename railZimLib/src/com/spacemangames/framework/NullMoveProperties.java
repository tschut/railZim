package com.spacemangames.framework;

import com.spacemangames.math.PointF;

public class NullMoveProperties implements IMoveProperties {

    PointF position = new PointF();

    @Override
    public void reset() {
    }

    @Override
    public void elapse(double elapsedMilliseconds) {
    }

    @Override
    public PointF getPos() {
        return position;
    }

}
