package com.spacemangames.library;

import com.spacemangames.framework.IMoveProperties;

public class SpacePlanetObject extends SpaceObject {
    private boolean mDOI;    // DOI = Death On Impact
    public float    mGravity;

    public SpacePlanetObject(String aBitmap, boolean lazyLoading, int aX, int aY, float aGrav, int aCollisionSize, boolean aDOI,
            IMoveProperties moveProperties) {
        super(aBitmap, lazyLoading, ObjectType.PLANET, aX, aY, aCollisionSize, moveProperties);

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
