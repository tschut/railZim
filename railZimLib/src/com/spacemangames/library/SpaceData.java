package com.spacemangames.library;

import java.util.ArrayList;
import java.util.HashSet;

import com.spacemangames.framework.EndGameState;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.ILevelChangedListener;
import com.spacemangames.framework.ILoadingDoneListener;
import com.spacemangames.framework.SpaceGamePoints;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.pal.PALManager;

public class SpaceData {
    public static final String                   TAG                   = SpaceData.class.getSimpleName();

    public ArrayList<SpaceLevel>                 levels;

    public ArrayList<SpaceLevel>                 specialLevels;

    public SpaceLevel                            currentLevel;

    public boolean                               preloadingDone;

    public SpaceGamePoints                       points;

    private final HashSet<ILoadingDoneListener>  loadingDoneListeners  = new HashSet<ILoadingDoneListener>();
    private final HashSet<ILevelChangedListener> levelChangedListeners = new HashSet<ILevelChangedListener>();

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

    public void setCurrentLevel(int index, boolean special) {
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
        }
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
