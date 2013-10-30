package com.spacemangames.gravisphere.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.spacemangames.framework.GameState;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.library.SpaceData;

class SpaceView extends SurfaceView implements SurfaceHolder.Callback {
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
        } else {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                gestureListener.onUp(event);
            }
            gestureDetector.onTouchEvent(event);
        }

        return true;
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
