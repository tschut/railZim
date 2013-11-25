package com.spacemangames.library;

import com.spacemangames.framework.CircularMoveProperties;
import com.spacemangames.math.PointF;

public class SpaceBackgroundObject extends SpaceObject {
    public SpaceBackgroundObject() {
        super(null, false, ObjectType.BACKGROUND, new PointF(), 0, new CircularMoveProperties());
    }
}
