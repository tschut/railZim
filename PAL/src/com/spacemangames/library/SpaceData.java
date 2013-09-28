package com.spacemangames.library;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.spacemangames.framework.ILevelChangedListener;
import com.spacemangames.framework.ILoadingDoneListener;
import com.spacemangames.framework.SpaceGamePoints;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.pal.PALManager;

public class SpaceData {
    public static final String                   TAG                    = "SpaceData";

    public static final float                    BOX2D_TIMESTEP         = 1.0f / 60.0f;                        // 60
                                                                                                                // fps

    public static final float                    BOX2D_PREDICT_TIMESTEP = 1.0f / 60.0f;                        // 30
                                                                                                                // fps
    public static final float                    PREDICTION_STEP        = 1.0f / 10f;                          // 5
                                                                                                                // per
                                                                                                                // second

    public static final float                    BOX2D_SPEEDUP          = 1.0f;                                // simulate
                                                                                                                // x
                                                                                                                // times
                                                                                                                // the
                                                                                                                // real
                                                                                                                // time
    public static final int                      BOX2D_POS_ITER         = 4;                                   // 10
                                                                                                                // =
                                                                                                                // suggested
                                                                                                                // by
                                                                                                                // manual
    public static final int                      BOX2D_VEL_ITER         = 2;                                   // 10
                                                                                                                // =
                                                                                                                // suggested
                                                                                                                // by
                                                                                                                // manual

    public static final int                      PREDICT_SECONDS        = 1;

    /** All levels loaded end up here */
    public ArrayList<SpaceLevel>                 mLevels;

    /** except these special ones :) */
    public ArrayList<SpaceLevel>                 mSpecialLevels;

    /** This is the current level */
    public SpaceLevel                            mCurrentLevel;

    /** Boolean indicating if preloading is finished */
    public boolean                               mPreloadingDone;

    /** Box2D stuff */
    private World                                mWorld;

    /** Object that keeps track of the points scored for the current level */
    public SpaceGamePoints                       mPoints;

    /** List of listeners for loading done event */
    private final HashSet<ILoadingDoneListener>  mLoadingDoneListeners  = new HashSet<ILoadingDoneListener>();
    /** List of listeners for change level event */
    private final HashSet<ILevelChangedListener> mLevelChangedListeners = new HashSet<ILevelChangedListener>();

    private SpaceData() {
        mPreloadingDone = false;
        mLevels = new ArrayList<SpaceLevel>();
        mSpecialLevels = new ArrayList<SpaceLevel>();
        mPoints = new SpaceGamePoints();
    }

    // Singleton holder
    private static class SingletonHolder {
        public static final SpaceData INSTANCE = new SpaceData();
    }

    // Singleton access
    public static SpaceData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public World getWorld() {
        return mWorld;
    }

    public void setCurrentLevel(int aIndex, boolean aSpecial) {
        // create a world without gravity
        mWorld = new World(new Vector2(0.0f, 0.0f), true);
        mWorld.setAutoClearForces(true);
        mWorld.setContactListener(SpaceWorldEventBuffer.getInstance().getContactListener());

        // reset the current level (if there is one), we may clean up resources
        // there
        if (mCurrentLevel != null) {
            mCurrentLevel.releaseLazyMemory();
            mCurrentLevel.reset();
        }

        // set the current level
        if (aSpecial) {
            mCurrentLevel = mSpecialLevels.get(aIndex);
        } else {
            mCurrentLevel = mLevels.get(aIndex);
        }
        mCurrentLevel.reset();
        // mCurrentLevel.dump();

        // reset points
        mPoints.reset();

        // create bodies for all objects
        int count = mCurrentLevel.mObjects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject lO = mCurrentLevel.mObjects.get(i);
            Body lBody = lO.createBody(mWorld);
            lO.reset();
        }

        // request garbage collection
        System.gc();

        // notify listeners
        invokeLevelChangedListeners(aIndex, aSpecial);
    }

    public void stepCurrentLevel(float aElapsed) {
        if (SpaceGameState.getInstance().getState() == SpaceGameState.STATE_FLYING) {
            // update points
            mPoints.elapse((int) (1000 * aElapsed));
        }

        // update physics
        updatePhysics(aElapsed);
    }

    public synchronized void updatePhysics(float aElapsed) {
        if (!SpaceGameState.getInstance().paused()) {
            // now run box2d simulation
            float lTimeCount = aElapsed;
            while (lTimeCount > BOX2D_TIMESTEP) {
                mCurrentLevel.updatePhysics(BOX2D_TIMESTEP);
                mWorld.step(BOX2D_TIMESTEP * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
                lTimeCount -= BOX2D_TIMESTEP;

                if (SpaceWorldEventBuffer.getInstance().forceStopEventHappened())
                    return;
            }

            if (lTimeCount > 0) {
                mCurrentLevel.updatePhysics(lTimeCount);
                mWorld.step(lTimeCount * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
            }

            int count = mCurrentLevel.mObjects.size();
            for (int i = 0; i < count; i++) {
                SpaceObject lObj = mCurrentLevel.mObjects.get(i);
                lObj.updatePositions();
            }
        }
    }

    public synchronized void resetPredictionData() {
        if (mCurrentLevel == null)
            return;

        SpaceManObject lSpaceMan = mCurrentLevel.getSpaceManObject();
        if (lSpaceMan != null)
            lSpaceMan.clearPredictionPoints();
    }

    public synchronized void calculatePredictionData(Vector2 aFirePower) {
        mCurrentLevel.setSpaceManSpeed(aFirePower);
        // now run box2d simulation
        float lSimulatedTime = 0;
        float lSinceLastPredictionStep = 0;
        int predictionIndex = 0;

        SpaceManObject lSpaceMan = mCurrentLevel.getSpaceManObject();
        resetPredictionData();
        while (lSimulatedTime <= PREDICT_SECONDS) {
            mCurrentLevel.updatePhysicsGravity(BOX2D_PREDICT_TIMESTEP);

            mWorld.step(BOX2D_PREDICT_TIMESTEP * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
            lSimulatedTime += BOX2D_PREDICT_TIMESTEP;
            lSinceLastPredictionStep += BOX2D_PREDICT_TIMESTEP;

            if (SpaceWorldEventBuffer.getInstance().forceStopEventHappened()) {
                SpaceWorldEventBuffer.getInstance().clear();
                break;
            }

            lSpaceMan.updatePositions();
            if (lSinceLastPredictionStep >= PREDICTION_STEP) {
                lSinceLastPredictionStep = 0;
                lSpaceMan.setPredictionPoint(predictionIndex);
                ++predictionIndex;
            }
        }
        lSpaceMan.reset();
    }

    public void Dump() {
        int lCount = mLevels.size();
        PALManager.getLog().i(TAG, "*************** Dumping level data ***************");
        for (int i = 0; i < lCount; i++) {
            PALManager.getLog().i(TAG, "Dumping level " + (i + 1) + " of " + lCount);
            mLevels.get(i).dump();
        }
        PALManager.getLog().i(TAG, "************* Dumping level data done ************");
    }

    public void preloadAllLevels() {
        PALManager.getResourceHandler().preloadAllLevels(mLevels);
        mCurrentLevel = mLevels.get(0);
        // done!
        // Dump results for debugging
        // Dump();
    }

    public void setLoadingDone() {
        mPreloadingDone = true;
        invokeLoadingDoneListeners();
    }

    public void setMainMenuLevel() {
        mCurrentLevel = mSpecialLevels.get(0);
    }

    public int getCurrentLevelId() {
        return mCurrentLevel.mId;
    }

    public int getLastLevelId() {
        return mLevels.get(mLevels.size() - 1).mId;
    }

    public int currentLevelWinState(int aPoints) {
        if (aPoints < mCurrentLevel.silver()) {
            return SpaceGameState.WON_BRONZE;
        } else if (aPoints < mCurrentLevel.gold()) {
            return SpaceGameState.WON_SILVER;
        } else {
            return SpaceGameState.WON_GOLD;
        }
    }

    public int levelStarColor(int aLevel, int aPoints) {
        assert aPoints > 0 : "if aPoints 0 we probably didn't finish the level yet";

        SpaceLevel level = mLevels.get(aLevel);
        if (aPoints >= level.gold()) {
            return SpaceGameState.WON_GOLD;
        } else if (aPoints >= level.silver()) {
            return SpaceGameState.WON_SILVER;
        } else if (aPoints > 0) {
            return SpaceGameState.WON_BRONZE;
        }
        return -1; // invalid?
    }

    public void addLoadingDoneListener(ILoadingDoneListener aListener) {
        synchronized (mLoadingDoneListeners) {
            mLoadingDoneListeners.add(aListener);
        }
    }

    public void addLevelChangedListener(ILevelChangedListener aListener) {
        synchronized (mLevelChangedListeners) {
            mLevelChangedListeners.add(aListener);
        }
    }

    public void remLoadingDoneListener(ILoadingDoneListener aListener) {
        synchronized (mLoadingDoneListeners) {
            mLoadingDoneListeners.remove(aListener);
        }
    }

    public void remLevelChangedListener(ILevelChangedListener aListener) {
        synchronized (mLevelChangedListeners) {
            mLevelChangedListeners.remove(aListener);
        }
    }

    private void invokeLoadingDoneListeners() {
        synchronized (mLoadingDoneListeners) {
            for (ILoadingDoneListener list : mLoadingDoneListeners) {
                list.loadingDone();
            }
        }
    }

    private void invokeLevelChangedListeners(int aNewLevelID, boolean aSpecial) {
        synchronized (mLevelChangedListeners) {
            for (ILevelChangedListener list : mLevelChangedListeners) {
                list.LevelChanged(aNewLevelID, aSpecial);
            }
        }
    }
}
