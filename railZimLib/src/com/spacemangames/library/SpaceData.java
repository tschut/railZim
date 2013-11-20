package com.spacemangames.library;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spacemangames.framework.EndGameState;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.ILevelChangedListener;
import com.spacemangames.framework.ILoadingDoneListener;
import com.spacemangames.framework.SpaceGamePoints;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.math.PointF;
import com.spacemangames.pal.PALManager;

public class SpaceData {
    public static final String                   TAG                    = SpaceData.class.getSimpleName();

    public static final float                    BOX2D_TIMESTEP         = 1.0f / 60.0f;

    public static final float                    BOX2D_PREDICT_TIMESTEP = 1.0f / 60.0f;
    public static final float                    PREDICTION_STEP        = 1.0f / 10f;
    public static final float                    BOX2D_SPEEDUP          = 1.0f;

    // box2d manual suggest 10 and 10 for these
    public static final int                      BOX2D_POS_ITER         = 4;
    public static final int                      BOX2D_VEL_ITER         = 2;

    public static final int                      PREDICT_SECONDS        = 1;

    public ArrayList<SpaceLevel>                 levels;

    public ArrayList<SpaceLevel>                 specialLevels;

    public SpaceLevel                            currentLevel;

    public boolean                               preloadingDone;

    private World                                world;

    public SpaceGamePoints                       points;

    private final HashSet<ILoadingDoneListener>  loadingDoneListeners   = new HashSet<ILoadingDoneListener>();
    private final HashSet<ILevelChangedListener> levelChangedListeners  = new HashSet<ILevelChangedListener>();

    private SpaceData() {
        preloadingDone = false;
        levels = new ArrayList<SpaceLevel>();
        specialLevels = new ArrayList<SpaceLevel>();
        points = new SpaceGamePoints();
    }

    private static class SingletonHolder {
        public static final SpaceData INSTANCE = new SpaceData();
    }

    public static SpaceData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public World getWorld() {
        return world;
    }

    public void setCurrentLevel(int index, boolean special) {
        world = new World(new Vector2(0.0f, 0.0f), true);
        world.setAutoClearForces(true);
        world.setContactListener(SpaceWorldEventBuffer.getInstance().getContactListener());

        if (currentLevel != null) {
            currentLevel.releaseLazyMemory();
            currentLevel.reset();
        }

        if (special) {
            currentLevel = specialLevels.get(index);
        } else {
            currentLevel = levels.get(index);
        }
        currentLevel.reset();

        points.reset();

        // create bodies for all objects
        int count = currentLevel.objects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject object = currentLevel.objects.get(i);
            object.createBody(world);
            object.reset();
        }

        invokeLevelChangedListeners(index, special);
    }

    public void stepCurrentLevel(float elapsed) {
        if (SpaceGameState.INSTANCE.getState() == GameState.FLYING) {
            points.elapse((int) (1000 * elapsed));
        }

        updatePhysics(elapsed);
    }

    public synchronized void updatePhysics(float elapsed) {
        if (!SpaceGameState.INSTANCE.paused()) {
            float timeCount = elapsed;
            while (timeCount > BOX2D_TIMESTEP) {
                currentLevel.updatePhysics(BOX2D_TIMESTEP);
                world.step(BOX2D_TIMESTEP * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
                timeCount -= BOX2D_TIMESTEP;

                if (SpaceWorldEventBuffer.getInstance().forceStopEventHappened())
                    return;
            }

            if (timeCount > 0) {
                currentLevel.updatePhysics(timeCount);
                world.step(timeCount * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
            }

            int count = currentLevel.objects.size();
            for (int i = 0; i < count; i++) {
                SpaceObject object = currentLevel.objects.get(i);
                object.updatePositions();
            }
        }
    }

    public synchronized void resetPredictionData() {
        if (currentLevel == null)
            return;

        SpaceManObject spaceMan = currentLevel.getSpaceManObject();
        if (spaceMan != null)
            spaceMan.clearPredictionPoints();
    }

    public synchronized void calculatePredictionData(PointF firePower) {
        currentLevel.setSpaceManSpeed(firePower);
        // now run box2d simulation
        float simulatedTime = 0;
        float sinceLastPredictionStep = 0;
        int predictionIndex = 0;

        SpaceManObject spaceMan = currentLevel.getSpaceManObject();
        resetPredictionData();
        while (simulatedTime <= PREDICT_SECONDS) {
            currentLevel.updatePhysicsGravity(BOX2D_PREDICT_TIMESTEP);

            world.step(BOX2D_PREDICT_TIMESTEP * BOX2D_SPEEDUP, BOX2D_VEL_ITER, BOX2D_POS_ITER);
            simulatedTime += BOX2D_PREDICT_TIMESTEP;
            sinceLastPredictionStep += BOX2D_PREDICT_TIMESTEP;

            if (SpaceWorldEventBuffer.getInstance().forceStopEventHappened()) {
                SpaceWorldEventBuffer.getInstance().clear();
                break;
            }

            spaceMan.updatePositions();
            if (sinceLastPredictionStep >= PREDICTION_STEP) {
                sinceLastPredictionStep = 0;
                spaceMan.setPredictionPoint(predictionIndex);
                ++predictionIndex;
            }
        }
        spaceMan.reset();
    }

    public void preloadAllLevels() {
        PALManager.getResourceHandler().preloadAllLevels(levels);
        currentLevel = levels.get(0);
    }

    public void setLoadingDone() {
        preloadingDone = true;
        invokeLoadingDoneListeners();
    }

    public void setMainMenuLevel() {
        currentLevel = specialLevels.get(0);
    }

    public int getCurrentLevelId() {
        return currentLevel.id;
    }

    public int getLastLevelId() {
        return levels.get(levels.size() - 1).id;
    }

    public EndGameState currentLevelWinState(int points) {
        if (points < currentLevel.silver()) {
            return EndGameState.WON_BRONZE;
        } else if (points < currentLevel.gold()) {
            return EndGameState.WON_SILVER;
        } else {
            return EndGameState.WON_GOLD;
        }
    }

    public EndGameState levelStarColor(int levelIndex, int points) {
        SpaceLevel level = levels.get(levelIndex);
        if (points >= level.gold()) {
            return EndGameState.WON_GOLD;
        } else if (points >= level.silver()) {
            return EndGameState.WON_SILVER;
        } else {
            return EndGameState.WON_BRONZE;
        }
    }

    public void addLoadingDoneListener(ILoadingDoneListener listener) {
        synchronized (loadingDoneListeners) {
            loadingDoneListeners.add(listener);
        }
    }

    public void addLevelChangedListener(ILevelChangedListener listener) {
        synchronized (levelChangedListeners) {
            levelChangedListeners.add(listener);
        }
    }

    public void remLoadingDoneListener(ILoadingDoneListener listener) {
        synchronized (loadingDoneListeners) {
            loadingDoneListeners.remove(listener);
        }
    }

    public void remLevelChangedListener(ILevelChangedListener listener) {
        synchronized (levelChangedListeners) {
            levelChangedListeners.remove(listener);
        }
    }

    private void invokeLoadingDoneListeners() {
        synchronized (loadingDoneListeners) {
            for (ILoadingDoneListener list : loadingDoneListeners) {
                list.loadingDone();
            }
        }
    }

    private void invokeLevelChangedListeners(int newLevelID, boolean special) {
        synchronized (levelChangedListeners) {
            for (ILevelChangedListener list : levelChangedListeners) {
                list.LevelChanged(newLevelID, special);
            }
        }
    }
}
