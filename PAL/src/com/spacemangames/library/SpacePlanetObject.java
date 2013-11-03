package com.spacemangames.library;

import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.math.PointF;

public class SpacePlanetObject extends SpaceObject {
    private boolean mDOI;    // DOI = Death On Impact
    public float    mGravity;

    public SpacePlanetObject(String aBitmap, boolean lazyLoading, PointF position, float aGrav, int aCollisionSize, boolean aDOI,
            IMoveProperties moveProperties) {
        super(aBitmap, lazyLoading, ObjectType.PLANET, position, aCollisionSize, moveProperties);

        mGravity = aGrav;
        mDOI = aDOI;
    }

    @Override
    public float gravity() {
        return mGravity;
    }

    @Override
    public boolean deathOnImpact() {
        return mDOI;
    }
}
