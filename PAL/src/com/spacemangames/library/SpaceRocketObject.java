package com.spacemangames.library;

import com.spacemangames.framework.IMoveProperties;

public class SpaceRocketObject extends SpaceObject {
    public SpaceRocketObject(String aBitmap, int aX, int aY, int aCollisionSize, IMoveProperties moveProperties) {
        super(aBitmap, false, TYPE_ROCKET, aX, aY, aCollisionSize, moveProperties);
    }

    @Override
    public void updateMoving(double aElapsed) {
        float angle;

        float curX = move.getPos().x;
        float curY = move.getPos().y;

        super.updateMoving(aElapsed);

        float newX = move.getPos().x;
        float newY = move.getPos().y;

        if (curX == newX && curY == newY) {
            angle = 0;
        } else {
            angle = (float) Math.PI / 2f + (float) Math.atan2(newY - curY, newX - curX);
        }

        if (body != null) {
            body.setTransform(body.getPosition(), angle);
        }
    }
}
