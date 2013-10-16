package com.spacemangames.library;

import com.spacemangames.framework.CircularMoveProperties;

public class SpaceRocketObject extends SpaceObject {
    public SpaceRocketObject(String aBitmap, int aX, int aY, int aCollisionSize, CircularMoveProperties aMove) {
        super(aBitmap, false, TYPE_ROCKET, aX, aY, aCollisionSize, aMove);
    }

    @Override
    public void updateMoving(double aElapsed) {
        float angle;

        float curX = mMove.getPos().x;
        float curY = mMove.getPos().y;

        super.updateMoving(aElapsed);

        float newX = mMove.getPos().x;
        float newY = mMove.getPos().y;

        if (curX == newX && curY == newY) {
            angle = 0;
        } else {
            angle = (float) Math.PI / 2f + (float) Math.atan2(newY - curY, newX - curX);
        }

        if (mBody != null) {
            mBody.setTransform(mBody.getPosition(), angle);
        }
    }
}
