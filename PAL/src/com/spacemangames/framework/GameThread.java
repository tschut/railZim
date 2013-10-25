package com.spacemangames.framework;

import java.util.LinkedList;
import java.util.List;

import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceObject;
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
    protected boolean            requestFireSpaceman       = false;
    public int                   canvasHeight              = 1;
    public int                   canvasWidth               = 1;

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
        float spaceManX = 0, spaceManY = 0;
        synchronized (viewport.getViewport()) {
            spaceManX = SpaceUtil.transformX(viewport.getViewport(), viewport.screenRect, spaceData.mCurrentLevel.getSpaceManObject().mX);
            spaceManY = SpaceUtil.transformY(viewport.getViewport(), viewport.screenRect, spaceData.mCurrentLevel.getSpaceManObject().mY);
        }

        double distance = Math.sqrt((x - spaceManX) * (x - spaceManX) + (y - spaceManY) * (y - spaceManY));

        if (distance <= SPACEMAN_HIT_FUZZYNESS * spaceData.mCurrentLevel.getSpaceManObject().getBitmap().getWidth()) {
            return true;
        }
        return false;
    }

    public SpaceObject objectUnderCursor(float x, float y) {
        List<SpaceObject> objects = spaceData.mCurrentLevel.mObjects;
        for (int i = 0; i < objects.size(); ++i) {
            SpaceObject object = objects.get(i);
            float objectX = SpaceUtil.transformX(viewport.getViewport(), viewport.screenRect, object.mX);
            float objectY = SpaceUtil.transformY(viewport.getViewport(), viewport.screenRect, object.mY);
            double distance = Math.sqrt((x - objectX) * (x - objectX) + (y - objectY) * (y - objectY));

            if (distance < object.getBitmap().getWidth() / 2)
                return object;
        }
        return null;
    }

    public boolean hitsSpaceManArrow(float x, float y) {
        Rect arrowRect = spaceData.mCurrentLevel.getSpaceManObject().getArrowData().mRect;
        if (arrowRect.isEmpty())
            return false;

        float arrowX = arrowRect.exactCenterX();
        float arrowY = arrowRect.exactCenterY();

        double distance = Math.sqrt((x - arrowX) * (x - arrowX) + (y - arrowY) * (y - arrowY));

        return distance <= ARROW_HIT_RADIUS;
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

    public synchronized void requestFireSpaceman() {
        requestFireSpaceman = true;
    }

    protected synchronized void fireSpaceMan() {
        spaceData.points.reset();
        SpaceGameState.INSTANCE.setState(GameState.FLYING);
        PointF speed = SpaceGameState.INSTANCE.chargingState.getSpaceManSpeed();
        spaceData.mCurrentLevel.setSpaceManSpeed(speed);

        viewport.setFocusOnSpaceman(true);
    }

    public void changeLevel(int index, boolean isSpecial) {
        synchronized (getSurfaceLocker()) {
            SpaceGameState.INSTANCE.chargingState.reset();
            spaceData.resetPredictionData();
            viewport.resetFocusViewportStatus(false);
            spaceData.setCurrentLevel(index, isSpecial);
            SpaceGameState.INSTANCE.setState(GameState.NOT_STARTED);
            SpaceGameState.INSTANCE.setEndState(EndGameState.NOT_ENDED);
            viewport.reset(spaceData.mCurrentLevel.startCenterX(), spaceData.mCurrentLevel.startCenterY(), canvasWidth, canvasHeight);
        }
    }

    public void loadNextLevel() {
        changeLevel(spaceData.getCurrentLevelId() + 1, false);
    }

    public void loadPrevLevel(boolean isSpecial) {
        changeLevel(spaceData.getCurrentLevelId() - 1, isSpecial);
    }

    public void loadNextLevel(boolean isSpecial) {
        changeLevel(spaceData.getCurrentLevelId() + 1, isSpecial);
    }

    public void reloadCurrentLevel() {
        changeLevel(spaceData.getCurrentLevelId(), false);
    }
}
