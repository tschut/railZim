package com.spacemangames.framework;

import java.util.LinkedList;
import java.util.List;

import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceObject;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;

public abstract class GameThread extends Thread {
    public static final String   TAG                       = "GameThread";
    public static final double   SPACEMAN_HIT_FUZZYNESS    = 1.4;
    public static final double   ARROW_HIT_RADIUS          = 50;

    /**
     * Start drawing the prediction if mChargingPower >
     * DRAW_PREDICTION_THRESHOLD
     */
    public static final float    DRAW_PREDICTION_THRESHOLD = 1f;

    public static final int      BONUS_POINTS              = 500;

    /** The data object */
    protected SpaceData          mSpaceData;
    /** Indicate whether the surface has been created & is ready to draw */
    protected boolean            mRun                      = false;
    public Viewport              mViewport                 = new Viewport();
    protected boolean            mRedrawOnce               = false;
    protected boolean            mRequestFireSpaceman      = false;
    /** Current height of the surface/canvas. */
    public int                   mCanvasHeight             = 1;
    /** Current width of the surface/canvas. */
    public int                   mCanvasWidth              = 1;

    private LinkedList<Runnable> mEventQueue;

    protected GameThread() {
        mSpaceData = SpaceData.getInstance();
        mEventQueue = new LinkedList<Runnable>();
    }

    public abstract Object getSurfaceLocker();

    protected void updatePhysics(float aElapsed) {
        mSpaceData.stepCurrentLevel(aElapsed);
    }

    public void setRunning(boolean b) {
        mRun = b;
    }

    public boolean hitsSpaceMan(float aX, float aY) {
        float lSpaceManX = 0, lSpaceManY = 0;
        synchronized (mViewport.getViewport()) {
            lSpaceManX = SpaceUtil.transformX(mViewport.getViewport(), mViewport.screenRect,
                    mSpaceData.mCurrentLevel.getSpaceManObject().mX);
            lSpaceManY = SpaceUtil.transformY(mViewport.getViewport(), mViewport.screenRect,
                    mSpaceData.mCurrentLevel.getSpaceManObject().mY);
        }

        double lDistance = Math.sqrt((aX - lSpaceManX) * (aX - lSpaceManX) + (aY - lSpaceManY) * (aY - lSpaceManY));

        if (lDistance <= SPACEMAN_HIT_FUZZYNESS * mSpaceData.mCurrentLevel.getSpaceManObject().getBitmap().getWidth()) {
            return true;
        }
        return false;
    }

    public SpaceObject objectUnderCursor(float aX, float aY) {
        List<SpaceObject> lObjects = mSpaceData.mCurrentLevel.mObjects;
        int lCount = lObjects.size();
        for (int i = 0; i < lCount; ++i) {
            SpaceObject lObject = lObjects.get(i);
            float lObjectX = SpaceUtil.transformX(mViewport.getViewport(), mViewport.screenRect, lObject.mX);
            float lObjectY = SpaceUtil.transformY(mViewport.getViewport(), mViewport.screenRect, lObject.mY);
            double lDistance = Math.sqrt((aX - lObjectX) * (aX - lObjectX) + (aY - lObjectY) * (aY - lObjectY));

            if (lDistance < lObject.getBitmap().getWidth() / 2)
                return lObject;
        }
        // no object under cursor, return null
        return null;
    }

    public boolean hitsSpaceManArrow(float aX, float aY) {
        Rect lArrowRect = mSpaceData.mCurrentLevel.getSpaceManObject().getArrowData().mRect;
        if (lArrowRect.isEmpty())
            return false;

        float lArrowX = lArrowRect.exactCenterX();
        float lArrowY = lArrowRect.exactCenterY();

        double lDistance = Math.sqrt((aX - lArrowX) * (aX - lArrowX) + (aY - lArrowY) * (aY - lArrowY));

        if (lDistance <= ARROW_HIT_RADIUS) {
            return true;
        }

        return false;
    }

    // returns immediately!
    public void postRunnable(Runnable runnable) {
        synchronized (mEventQueue) {
            mEventQueue.add(runnable);
        }
    }

    public void postSyncRunnable(Runnable runnable) {
        postRunnable(runnable);
        while (mEventQueue.contains(runnable)) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void runQueue() {
        synchronized (mEventQueue) {
            while (mEventQueue.size() > 0) {
                Runnable runnable = mEventQueue.remove();
                if (runnable != null)
                    runnable.run();
            }
        }
    }

    public void redrawOnce() {
        mRedrawOnce = true;
    }

    public synchronized void requestFireSpaceman() {
        mRequestFireSpaceman = true;
    }

    protected synchronized void fireSpaceMan() {
        mSpaceData.points.reset();
        SpaceGameState.INSTANCE.setState(GameState.FLYING);
        PointF speed = SpaceGameState.INSTANCE.chargingState.getSpaceManSpeed();
        mSpaceData.mCurrentLevel.setSpaceManSpeed(speed);

        mViewport.setFocusOnSpaceman(true);
    }

    public void changeLevel(int aIndex, boolean aSpecial) {
        synchronized (getSurfaceLocker()) {
            SpaceGameState.INSTANCE.chargingState.reset();
            SpaceData.getInstance().resetPredictionData();
            mViewport.resetFocusViewportStatus(false);
            mSpaceData.setCurrentLevel(aIndex, aSpecial);
            SpaceGameState.INSTANCE.setState(GameState.NOT_STARTED);
            SpaceGameState.INSTANCE.setEndState(EndGameState.NOT_ENDED);
            mViewport.reset(mSpaceData.mCurrentLevel.startCenterX(), mSpaceData.mCurrentLevel.startCenterY(), mCanvasWidth, mCanvasHeight);
        }
    }

    public void loadPrevLevel() {
        changeLevel(SpaceData.getInstance().getCurrentLevelId() - 1, false);
    }

    public void loadNextLevel() {
        changeLevel(SpaceData.getInstance().getCurrentLevelId() + 1, false);
    }

    public void loadPrevLevel(boolean aSpecial) {
        changeLevel(SpaceData.getInstance().getCurrentLevelId() - 1, aSpecial);
    }

    public void loadNextLevel(boolean aSpecial) {
        changeLevel(SpaceData.getInstance().getCurrentLevelId() + 1, aSpecial);
    }

    public void reloadCurrentLevel() {
        changeLevel(SpaceData.getInstance().getCurrentLevelId(), false);
    }
}
