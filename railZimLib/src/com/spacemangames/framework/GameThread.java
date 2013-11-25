package com.spacemangames.framework;

import java.util.LinkedList;

import com.spacemangames.library.SpaceData;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;

public abstract class GameThread extends Thread {
    public static final String   TAG                       = GameThread.class.getSimpleName();
    public static final double   SPACEMAN_HIT_FUZZYNESS    = 1.4;
    public static final double   ARROW_HIT_RADIUS          = 50;

    public static final float    DRAW_PREDICTION_THRESHOLD = 1f;

    public static final int      BONUS_POINTS              = 500;

    protected SpaceData          spaceData;
    protected boolean            running                   = false;
    public Viewport              viewport                  = new Viewport();
    protected boolean            redrawOnce                = false;
    protected Rect               canvasSize                = new Rect(0, 0, 1, 1);

    private LinkedList<Runnable> eventQueue;

    protected GameThread(SpaceData spaceData) {
        this.spaceData = spaceData;
        eventQueue = new LinkedList<Runnable>();
    }

    public abstract Object getSurfaceLocker();

    protected void updatePhysics(float elapsed) {
        spaceData.stepCurrentLevel(elapsed);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean hitsSpaceMan(float x, float y) {
        PointF spacemanPosition = viewport.toScreenCoordinates(spaceData.currentLevel.getSpaceManObject().getPosition());
        double distance = spacemanPosition.distanceTo(new PointF(x, y));

        return distance <= SPACEMAN_HIT_FUZZYNESS * spaceData.currentLevel.getSpaceManObject().getBitmap().getWidth();
    }

    // returns immediately!
    public void postRunnable(Runnable runnable) {
        synchronized (eventQueue) {
            eventQueue.add(runnable);
        }
    }

    public void postSyncRunnable(Runnable runnable) {
        postRunnable(runnable);
        while (eventQueue.contains(runnable)) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void runQueue() {
        synchronized (eventQueue) {
            while (eventQueue.size() > 0) {
                Runnable runnable = eventQueue.remove();
                if (runnable != null)
                    runnable.run();
            }
        }
    }

    public void redrawOnce() {
        redrawOnce = true;
    }

    public void changeLevel(int index) {
        synchronized (getSurfaceLocker()) {
            SpaceGameState.INSTANCE.chargingState.reset();
            viewport.resetFocusViewportStatus(false);
            spaceData.setCurrentLevel(index);
            SpaceGameState.INSTANCE.setState(GameState.NOT_STARTED);
            SpaceGameState.INSTANCE.setEndState(EndGameState.NOT_ENDED);
            viewport.reset(spaceData.currentLevel.startCenter(), canvasSize);
        }
    }

    public void loadNextLevel() {
        changeLevel(spaceData.getCurrentLevelId() + 1);
    }

    public void loadPrevLevel() {
        changeLevel(spaceData.getCurrentLevelId() - 1);
    }

    public void reloadCurrentLevel() {
        changeLevel(spaceData.getCurrentLevelId());
    }
}
