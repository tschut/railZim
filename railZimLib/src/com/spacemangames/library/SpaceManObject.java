package com.spacemangames.library;

import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.math.PointF;
import com.spacemangames.pal.IRenderer;

public class SpaceManObject extends SpaceObject {
    public SpaceManObject(String aBitmap, PointF position,
            String aArrowResource, int aCollisionSize,
            IMoveProperties moveProperties) {
        super(aBitmap, false, ObjectType.SPACEMAN, position, aCollisionSize,
                moveProperties);
    }

    @Override
    public void dispatchToRenderer(IRenderer aRenderer) {
        aRenderer.doDraw(this);
    }
}
