package com.spacemangames.library;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.MoveProperties;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public class SpaceLevel {
    public static final String    MTAG              = "SpaceLevel";

    public static final int       ID_LOADING_SCREEN = 0;
    public static final int       ID_HELP1          = 1;
    public static final int       ID_HELP2          = 2;
    public static final int       ID_HELP3          = 3;
    public static final int       ID_HELP4          = 4;

    /** list of objects in this level */
    public ArrayList<SpaceObject> mObjects;
    /** The background (which is a special object) */
    public SpaceBackgroundObject  mBackgroundObject;

    /** Id of this level */
    public int                    mId;

    /** Bitmap used for prediction */
    public IBitmap                mPredictionBitmap;

    /** Name of this level */
    public String                 mName;

    private int                   mStartY;
    private int                   mStartX;

    /** Point you have to score for silver/gold medals */
    private int                   mSilver;
    private int                   mGold;

    /** scratch variables */
    public static Vector2         mScratchVector1   = new Vector2();
    public static Vector2         mScratchVector2   = new Vector2();
    public static Vector2         mScratchVector3   = new Vector2();

    private final Vector2         mSpaceManSpeedBuf = new Vector2();

    public SpaceLevel() {
        // Note: 16 is the initial value, we can grow beyond that. However, it
        // seems a nice estimate,
        // so as long as we stay below that we should have better loading
        // performance as the array doesn't
        // have to grow.
        mObjects = new ArrayList<SpaceObject>(16);
    }

    public void draw(IRenderer aRenderer) {
        aRenderer.doDraw(mObjects, mBackgroundObject);
    }

    public void dump() {
        int lObjCount = mObjects.size();
        int lObjIndex = 0;

        PALManager.getLog().i(MTAG, "******" + mName + "******");
        PALManager.getLog().i(MTAG, "Startcenter: " + mStartX + ":" + mStartY);
        PALManager.getLog().i(MTAG, "Silver: " + mSilver + " Gold: " + mGold);
        PALManager.getLog().i(MTAG, "This level contains " + lObjCount + " objects");
        for (SpaceObject lObj : mObjects) {
            PALManager.getLog().i(MTAG, "Object: " + lObjIndex);
            PALManager.getLog().i(MTAG, lObj.toString());
            lObjIndex++;
        }
    }

    public SpaceManObject getSpaceManObject() {
        int count = mObjects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject lO = mObjects.get(i);
            if (lO.mType == SpaceObject.TYPE_SPACEMAN)
                return (SpaceManObject) lO;
        }
        // should not get here! (can get here during loading though!)
        PALManager.getLog().e("SpaceObject", "Could not find spaceman object!");
        return null;
    }

    public void addSpaceMan(int aX, int aY, String aBitmap, String aArrowBitmap, int aCollisionSize, MoveProperties aMoveProperties) {
        SpaceManObject lObject = new SpaceManObject(aBitmap, aX, aY, aArrowBitmap, aCollisionSize, aMoveProperties);
        mObjects.add(lObject);
    }

    public void addPlanet(int aX, int aY, String aBitmap, boolean lazyLoading, float aGrav, int aCollisionSize, boolean aDOI,
            MoveProperties aMove) {
        SpacePlanetObject lObject = new SpacePlanetObject(aBitmap, lazyLoading, aX, aY, aGrav, aCollisionSize, aDOI, aMove);
        mObjects.add(lObject);
    }

    public void addRocket(int aX, int aY, String aBitmap, int aCollisionSize, MoveProperties aMove) {
        SpaceRocketObject lObject = new SpaceRocketObject(aBitmap, aX, aY, aCollisionSize, aMove);
        mObjects.add(lObject);
    }

    public void addBonus(int aX, int aY, String aBitmap, int aCollisionSize, MoveProperties aMove) {
        SpaceBonusObject lObject = new SpaceBonusObject(aBitmap, aX, aY, aCollisionSize, aMove);
        mObjects.add(lObject);
    }

    public void setSpaceManSpeed(Vector2 aSpeed) {
        mSpaceManSpeedBuf.set(aSpeed.x, aSpeed.y);
        getSpaceManObject().setSpeed(mSpaceManSpeedBuf);
    }

    public void updatePhysics(float aElapsed) {
        if (SpaceGameState.getInstance().getState() == GameState.FLYING) {
            // calculate gravitational effect for all relevant objects
            updatePhysicsGravity(aElapsed);
        }
        updateMovingObjects(aElapsed);
    }

    public void reset() {
        int count = mObjects.size();
        for (int i = 0; i < count; i++) {
            // by doing this we force the correct rotation for objects that move
            // and rotate with that (e.g. the rocket)
            mObjects.get(i).updateMoving(1);
            mObjects.get(i).reset();
        }
    }

    private void updateMovingObjects(float aElapsed) {
        int count = mObjects.size();
        for (int i = 0; i < count; i++) {
            mObjects.get(i).updateMoving(aElapsed);
        }
    }

    public void updatePhysicsGravity(float aElapsed) {
        int count = mObjects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject lO1 = mObjects.get(i);
            if (lO1.isAffectedByGravity()) {
                for (int j = 0; j < count; j++) {
                    SpaceObject lO2 = mObjects.get(j);
                    updatePhysicsGravity(aElapsed, lO1, lO2);
                }
            }
        }
    }

    private void updatePhysicsGravity(float aElapsed, SpaceObject aO1, SpaceObject aO2) {
        float lGrav = aO2.gravity();
        if (lGrav > 0.f) {
            // Note: we apply some scaling to keep values a bit normal
            mScratchVector1.set(aO1.mBody.getPosition());
            mScratchVector2.set(aO2.mBody.getPosition());
            float lDistance = mScratchVector1.dst(mScratchVector2);
            if (lDistance > 0.0) { // if distance == 0, lO1 == lO2
                mScratchVector3.set(mScratchVector2); // vec3 = vec2
                mScratchVector3.sub(mScratchVector1); // vec3 = vec2 - vec1
                mScratchVector1.set(mScratchVector3); // vec1 = vec3
                mScratchVector1.mul(1.0f / mScratchVector3.len()); // vec1 =
                                                                   // normalized
                                                                   // direction
                // grav pull, inverse square relationship with distance
                float lGravPull = (lGrav / (lDistance * lDistance)) * aElapsed;
                mScratchVector1.mul(lGravPull);
                aO1.applyForce(mScratchVector1, aO1.mBody.getPosition());
            }
        }
    }

    public void addBackground(String aBackgroundColorInner, String aBackgroundColorOuter) {
        mBackgroundObject = new SpaceBackgroundObject(aBackgroundColorInner, aBackgroundColorOuter, mName);
    }

    public void setStartCenterY(int lStartY) {
        mStartY = lStartY;
    }

    public void setStartCenterX(int lStartX) {
        mStartX = lStartX;
    }

    public int startCenterX() {
        return mStartX;
    }

    public int startCenterY() {
        return mStartY;
    }

    public void setPredictionBitmap(String aResource) {
        mPredictionBitmap = PALManager.getBitmapFactory().createBitmap(aResource, false);
    }

    public void setSilver(int aSilver) {
        mSilver = aSilver;
    }

    public void setGold(int aGold) {
        mGold = aGold;
    }

    public int silver() {
        return mSilver;
    }

    public int gold() {
        return mGold;
    }

    public void releaseLazyMemory() {
        for (SpaceObject object : mObjects) {
            object.releaseLazyMemory();
        }
    }
}
