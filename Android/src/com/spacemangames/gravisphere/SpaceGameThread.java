package com.spacemangames.gravisphere;

import java.util.Queue;

import android.graphics.Canvas;
import android.os.Handler;
import android.util.FloatMath;
import android.view.SurfaceHolder;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.spacemangames.framework.EndGameState;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.GameThread;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.gravisphere.pal.AndroidRenderer;
import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceWorldEventBuffer;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.PALManager;
import com.spacemangames.util.ThreadUtils;

public class SpaceGameThread extends GameThread {
    public class FireSpacemanRunnable implements Runnable {
        @Override
        public void run() {
            fireSpaceMan();
        }
    }

    public static final String           TAG                = SpaceGameThread.class.getSimpleName();

    public static final float            MIN_FRAME_TIME     = 0.033f;                               // in
                                                                                                     // seconds
                                                                                                     // (0.033
                                                                                                     // =
                                                                                                     // 30
                                                                                                     // fps)
    public static final float            MAX_FRAME_TIME     = 0.100f;

    private SurfaceHolder                surfaceHolder;
    private final Object                 dummySurfaceHolder = new Object();

    // used to message the ui thread
    private Handler                      msgHandler;

    // The rendering engine
    private final AndroidRenderer        renderer;

    private boolean                      frozen             = false;

    private final GoogleAnalyticsTracker tracker;

    public SpaceGameThread(SpaceData spaceData) {
        super(spaceData);
        SpaceGameState.INSTANCE.setState(GameState.LOADING);

        viewport.setFlingSpeed(new PointF());
        renderer = new AndroidRenderer();
        tracker = GoogleAnalyticsTracker.getInstance();
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        if (surfaceHolder != null) {
            synchronized (surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
            }
        } else {
            this.surfaceHolder = surfaceHolder;
        }
    }

    @Override
    public Object getSurfaceLocker() {
        if (surfaceHolder != null) {
            return (Object) surfaceHolder;
        } else {
            return dummySurfaceHolder;
        }
    }

    public void freeze() {
        PALManager.getLog().v(TAG, "Freezing thread");
        frozen = true;
    }

    public void unfreeze() {
        PALManager.getLog().v(TAG, "Unfreezing thread");
        frozen = false;
    }

    @Override
    public void run() {
        SpaceGameState gameState = SpaceGameState.INSTANCE;
        Rect viewportCopy = new Rect();
        gameState.updateTimeTick();
        while (running) {
            runQueue();

            boolean viewportValid = safeCopyViewport(viewportCopy);

            if (shouldRunRegularFrame(gameState, viewportValid)) {
                long loopStart = System.nanoTime();
                float elapsed = gameState.tick();

                parseGameEvents();
                updatePhysics(elapsed);
                viewport.update(elapsed);
                updatePrediction(gameState);

                Canvas canvas = drawToCanvas(viewportCopy);

                sleepUntilFrameFilled(loopStart);

                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            } else if (shouldRedrawOnce()) {
                Canvas canvas = surfaceHolder.lockCanvas(null);
                if (canvas != null) {
                    redrawOnce = false;
                    synchronized (surfaceHolder) {
                        doDraw(canvas, viewportCopy);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                } else {
                    ThreadUtils.silentSleep(10);
                }
            } else {
                ThreadUtils.silentSleep(100);
            }
        }
    }

    private boolean shouldRedrawOnce() {
        return redrawOnce && surfaceHolder != null && !frozen;
    }

    private boolean shouldRunRegularFrame(SpaceGameState gameState, boolean viewportValid) {
        return !gameState.paused() && viewportValid && !frozen;
    }

    private Canvas drawToCanvas(Rect viewportCopy) {
        Canvas canvas = surfaceHolder.lockCanvas(null);
        if (canvas != null) {
            synchronized (surfaceHolder) {
                try {
                    doDraw(canvas, viewportCopy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return canvas;
    }

    private void sleepUntilFrameFilled(long loopStart) {
        double upToHere = (System.nanoTime() - loopStart) / 1000000000d;
        if (upToHere < MIN_FRAME_TIME)
            ThreadUtils.silentSleep((long) ((MIN_FRAME_TIME - upToHere) * 1000));
    }

    private void updatePrediction(SpaceGameState gameState) {
        if (gameState.chargingState.chargingPower() > DRAW_PREDICTION_THRESHOLD && gameState.getState() == GameState.CHARGING) {
            gameState.setPredicting(true);
            spaceData.calculatePredictionData(SpaceGameState.INSTANCE.chargingState.getSpaceManSpeed());
            gameState.setPredicting(false);
        }
    }

    private boolean safeCopyViewport(Rect viewportCopy) {
        boolean result = false;
        synchronized (viewport.getViewport()) {
            if (viewport.isValid()) {
                result = true;
                viewportCopy.set(viewport.getViewport());
            }
        }
        return result;
    }

    public void setSurfaceSize(int width, int height) {
        PALManager.getLog().i(TAG, "surface size " + width + "x" + height);
        synchronized (surfaceHolder) {
            canvasSize.set(0, 0, width, height);
            if (spaceData.mCurrentLevel != null) {
                viewport.reset(spaceData.mCurrentLevel.startCenter(), canvasSize);
            } else {
                viewport.reset(viewport.getViewport().center(), canvasSize);
            }
            redrawOnce();
        }
    }

    private void doDraw(Canvas canvas, Rect viewportRect) {
        if (SpaceGameState.INSTANCE.getState().isDoneLoading()) {
            renderer.initialize(canvas, viewportRect, viewport.screenRect);
            spaceData.mCurrentLevel.draw(renderer);
        }
    }

    private void parseGameEvents() {
        // check if points have reached zero
        if (SpaceData.getInstance().points.getCurrentPoints() == 0) {
            tracker.trackEvent("out-of-time", String.valueOf(SpaceData.getInstance().getCurrentLevelId()), "", 0);
            SpaceGameState.INSTANCE.setPaused(true);
            SpaceGameState.INSTANCE.setEndState(EndGameState.LOST_LOST);
            msgHandler.sendEmptyMessage(0);
        }

        // check world events
        Queue<Integer> lQueue = SpaceWorldEventBuffer.getInstance().mEvents;
        while (lQueue.size() > 0) {
            Integer lEvent = lQueue.remove();
            switch (lEvent) {
            case (SpaceWorldEventBuffer.EVENT_HIT_ROCKET):
                SpaceGameState.INSTANCE.setPaused(true);
                int lCurrentLevelID = SpaceData.getInstance().getCurrentLevelId();
                int lHighScore = LevelDbAdapter.getInstance().highScore(lCurrentLevelID);
                int lCurScore = SpaceData.getInstance().points.getCurrentPoints();
                SpaceGameState.INSTANCE.setEndState(SpaceData.getInstance().currentLevelWinState(lCurScore));
                tracker.trackEvent("win", String.valueOf(lCurrentLevelID), String.valueOf(lCurScore), 0);

                if (lCurScore > lHighScore)
                    LevelDbAdapter.getInstance().updateHighScore(lCurrentLevelID, lCurScore);
                msgHandler.sendEmptyMessage(0);
                break;
            case (SpaceWorldEventBuffer.EVENT_HIT_DOI_OBJECT):
                tracker.trackEvent("die", String.valueOf(SpaceData.getInstance().getCurrentLevelId()), "", 0);
                SpaceGameState.INSTANCE.setPaused(true);
                SpaceGameState.INSTANCE.setEndState(EndGameState.LOST_DIE);
                msgHandler.sendEmptyMessage(0);
                break;
            case (SpaceWorldEventBuffer.EVENT_SCORE_BONUS):
                SpaceData.getInstance().points.bonus(BONUS_POINTS);
                break;
            default:
                PALManager.getLog().e(TAG, "Unexpected game event");
            }
        }
    }

    public float canvasDiagonal() {
        return FloatMath.sqrt(canvasSize.width() * canvasSize.width() + canvasSize.height() * canvasSize.height());
    }

    public void setMsgHandler(Handler msgHandler) {
        this.msgHandler = msgHandler;
    }
}