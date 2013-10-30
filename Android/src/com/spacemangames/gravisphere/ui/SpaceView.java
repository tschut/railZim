package com.spacemangames.gravisphere.ui;

import java.util.Vector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.gravisphere.SpaceGameThread;
import com.spacemangames.library.SpaceData;

class SpaceView extends SurfaceView implements SurfaceHolder.Callback {
    /** Used to have an offset while dragging the view around */
    private Vector2              dragStart;
    private boolean              dragging             = false;
    private static final int     MIN_MOVE_BEFORE_DRAG = 20;                        // pixels
    /** Variables used to implement flinging */
    private static final int     MIN_SPEED_FOR_FLING  = 100;                       // 100
                                                                                    // pixels/second
    private static final int     MAX_FLING_SPEED      = 2000;
    private static final long    MAX_TIME_FOR_FLING   = 300 * 1000 * 1000;         // 300
                                                                                    // milliseconds
    private static final int     ACCUMULATE_COUNT     = 3;
    private long                 previousTime;
    private Vector<Vector2>      previousLocations;
    private int                  indexInVector;

    private GestureDetector      gestureDetector;
    private GestureListener      gestureListener      = new GestureListener();

    private ScaleGestureDetector scaleGestureDetector;
    private ScaleGestureListener scaleGestureListener = new ScaleGestureListener();

    // if this is true all input is ignored
    private boolean              ignoreInput          = false;

    public boolean               ignoreFocusChange    = false;

    public SpaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector = new GestureDetector(context, gestureListener);

        dragStart = new Vector2();
        previousLocations = new Vector<Vector2>();
        for (int i = 0; i < ACCUMULATE_COUNT; i++) {
            previousLocations.add(new Vector2(0, 0));
        }

        setFocusable(true); // make sure we get key events

        getHolder().addCallback(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (ignoreFocusChange)
            return;

        SpaceGameState gameState = SpaceGameState.INSTANCE;
        if (!hasWindowFocus) {
            gameState.setPaused(true);
            gameState.chargingState.reset();
        } else {
            gameState.setPaused(false);
        }
    }

    public void ignoreInput(boolean ignore) {
        ignoreInput = ignore;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (inputShouldBeIgnored()) {
            return false;
        }

        scaleGestureDetector.onTouchEvent(event);
        if (scaleGestureDetector.isInProgress()) {
            interruptCharging();
            return true;
        }

        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            gestureListener.onUp(event);
        }

        SpaceGameThread gameThread = GameThreadHolder.getThread();
        GameState state = SpaceGameState.INSTANCE.getState();

        // only process input if we're in the right state
        if (state != GameState.CHARGING && state != GameState.FLYING && state != GameState.NOT_STARTED) {
            return true;
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        boolean hitsSpaceMan = gameThread.hitsSpaceMan(x, y);

        // reset the focus viewport stuff
        gameThread.viewport.resetFocusViewportStatus(false);

        // This means we are starting with the charging (if this is on the
        // location of spaceman)
        if (state == GameState.NOT_STARTED && action == MotionEvent.ACTION_DOWN && hitsSpaceMan) {
            result = true;
        } else if (state == GameState.CHARGING) {
            x = SpaceUtil.resolutionScale(x);
            y = SpaceUtil.resolutionScale(y);
            if (action == MotionEvent.ACTION_MOVE) {
                SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            }
            result = true;
        } else if (action == MotionEvent.ACTION_DOWN) {
            gameThread.viewport.setFlinging(false);
            dragStart.set(x, y);
            gameThread.viewport.startViewportDrag(x, y);
            indexInVector = ACCUMULATE_COUNT - 1;
            previousLocations.get(indexInVector).set(x, y);
            gameThread.viewport.getFlingSpeed().set(0, 0);
            result = true;
        } else if (action == MotionEvent.ACTION_MOVE) { // moving the viewport
            if (dragStart.dst(x, y) > MIN_MOVE_BEFORE_DRAG || dragging) {
                dragging = true;
                gameThread.viewport.dragViewport(x, y);
                long currentTime = System.nanoTime();
                float dT = (float) ((currentTime - previousTime) / 1000000000d);
                gameThread.viewport.getFlingSpeed().x = (previousLocations.get(indexInVector).x - x) / dT;
                gameThread.viewport.getFlingSpeed().y = (previousLocations.get(indexInVector).y - y) / dT;
                // shift one left
                for (int i = 0; i < ACCUMULATE_COUNT - 1; i++) {
                    previousLocations.get(i).set(previousLocations.get(i + 1));
                }
                previousLocations.get(indexInVector).set(x, y);
                previousTime = currentTime;
                indexInVector--;
                if (indexInVector < 0) {
                    indexInVector = 0;
                }
            }
            result = true;
        } else if (action == MotionEvent.ACTION_UP) {
            dragging = false;
            gameThread.viewport.stopViewportDrag();
            long currentTime = System.nanoTime();
            float len = gameThread.viewport.getFlingSpeed().length();
            if (len > MIN_SPEED_FOR_FLING && currentTime - previousTime < MAX_TIME_FOR_FLING) {
                if (len > MAX_FLING_SPEED) {
                    gameThread.viewport.getFlingSpeed().x = (gameThread.viewport.getFlingSpeed().x / len) * MAX_FLING_SPEED;
                    gameThread.viewport.getFlingSpeed().y = (gameThread.viewport.getFlingSpeed().y / len) * MAX_FLING_SPEED;
                }
                gameThread.viewport.setFlinging(true);
            }
            result = true;
        }

        return result;
    }

    private void interruptCharging() {
        if (SpaceGameState.INSTANCE.getState() == GameState.CHARGING) {
            SpaceGameState.INSTANCE.chargingState.reset();
            SpaceData.getInstance().resetPredictionData();
            SpaceGameState.INSTANCE.setState(GameState.NOT_STARTED);
        }
    }

    private boolean inputShouldBeIgnored() {
        return ignoreInput || GameThreadHolder.getThread() == null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        GameThreadHolder.getThread().setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }
}
