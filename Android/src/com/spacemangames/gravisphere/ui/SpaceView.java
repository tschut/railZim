package com.spacemangames.gravisphere.ui;

import java.util.Vector;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.gravisphere.SpaceGameThread;
import com.spacemangames.library.SpaceData;
import com.spacemangames.pal.PALManager;

class SpaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String MTAG                 = "SpaceView";

    /** Used to have an offset while dragging the view around */
    private Vector2             dragStart;
    private boolean             dragging             = false;
    private static final int    MIN_MOVE_BEFORE_DRAG = 20;               // pixels
    /** Variables used to implement flinging */
    private static final int    MIN_SPEED_FOR_FLING  = 100;              // 100
                                                                          // pixels/second
    private static final int    MAX_FLING_SPEED      = 2000;
    private static final long   MAX_TIME_FOR_FLING   = 300 * 1000 * 1000; // 300
                                                                          // milliseconds
    private static final int    ACCUMULATE_COUNT     = 3;
    private long                previousTime;
    private Vector<Vector2>     previousLocations;
    private int                 indexInVector;
    /** Variables used to implement pinch-zoom */
    private static final int    MIN_MOVE_BEFORE_ZOOM = 20;               // pixels
    private float               previousDist;
    private boolean             zooming;

    // if this is true all input is ignored
    private boolean             ignoreInput          = false;

    public boolean              ignoreFocusChange    = false;

    public SpaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

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

        SpaceGameThread gameThread = GameThreadHolder.getThread();
        GameState state = SpaceGameState.INSTANCE.getState();

        // only process input if we're in the right state
        if (state != GameState.CHARGING && state != GameState.FLYING && state != GameState.NOT_STARTED) {
            return true;
        }

        // check for multi-touch input
        if (event.getPointerCount() > 1) {
            interruptCharging();
            handleMultitouchEvent(event);
            return true;
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        boolean hitsSpaceMan = gameThread.hitsSpaceMan(x, y);
        boolean hitsArrow = gameThread.hitsSpaceManArrow(x, y);

        // reset the focus viewport stuff
        gameThread.viewport.resetFocusViewportStatus(false);

        // this can be when the user was pinch-zooming and took only one finger
        // off the screen
        if (zooming) {
            action = MotionEvent.ACTION_UP;
            hitsSpaceMan = false;
            zooming = false;
        }

        // This means we are starting with the charging (if this is on the
        // location of spaceman)
        if (state == GameState.NOT_STARTED && action == MotionEvent.ACTION_DOWN && hitsSpaceMan) {
            x = SpaceUtil.resolutionScale(x);
            y = SpaceUtil.resolutionScale(y);
            SpaceGameState.INSTANCE.setState(GameState.CHARGING);
            SpaceGameState.INSTANCE.chargingState.setChargingStart(x, y);
            SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            result = true;
        } else if (state == GameState.CHARGING) {
            x = SpaceUtil.resolutionScale(x);
            y = SpaceUtil.resolutionScale(y);
            if (action == MotionEvent.ACTION_MOVE) {
                SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            } else if (action == MotionEvent.ACTION_UP) {
                SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
                gameThread.postRunnable(gameThread.new FireSpacemanRunnable());
            }
            result = true;
        } else if (state == GameState.NOT_STARTED && action == MotionEvent.ACTION_DOWN && hitsArrow) {
            gameThread.viewport.focusOn(SpaceData.getInstance().mCurrentLevel.getSpaceManObject().getPosition());
        } else if (action == MotionEvent.ACTION_DOWN && hitsArrow) {
            gameThread.viewport.resetFocusViewportStatus(true);
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

    private void handleMultitouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        PALManager.getLog().i(MTAG, "Multitouch event. Pointers: " + pointerCount);

        if (pointerCount != 2)
            return;

        float dx = Math.abs(event.getX(0) - event.getX(1));
        float dy = Math.abs(event.getY(0) - event.getY(1));
        float dist = FloatMath.sqrt(dx * dx + dy * dy);

        switch (event.getAction()) {
        case MotionEvent.ACTION_POINTER_1_DOWN: // either the first
        case MotionEvent.ACTION_POINTER_2_DOWN: // or second pointer has gone
                                                // down
            PALManager.getLog().i(MTAG, "Action down");
            zooming = false;
            previousDist = dist;
            break;
        case MotionEvent.ACTION_MOVE:
            PALManager.getLog().i(MTAG, "Action move");
            float zoom = previousDist - dist;
            if (Math.abs(zoom) > MIN_MOVE_BEFORE_ZOOM || zooming) {
                zoom = (zoom / GameThreadHolder.getThread().canvasDiagonal());
                GameThreadHolder.getThread().viewport.zoomViewport(zoom);
                previousDist = dist;
                zooming = true;
            }
        default:
            PALManager.getLog().i(MTAG, "Action: " + event.getAction());
        }
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
