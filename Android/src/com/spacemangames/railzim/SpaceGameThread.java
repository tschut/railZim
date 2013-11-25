package com.spacemangames.railzim;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.spacemangames.framework.GameState;
import com.spacemangames.framework.GameThread;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.library.SpaceData;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.pal.AndroidRenderer;
import com.spacemangames.util.ThreadUtils;

public class SpaceGameThread extends GameThread {
    public static final String    TAG                = SpaceGameThread.class.getSimpleName();

    public static final float     MIN_FRAME_TIME     = 0.033f;
    public static final float     MAX_FRAME_TIME     = 0.100f;

    private SurfaceHolder         surfaceHolder;
    private final Object          dummySurfaceHolder = new Object();

    private final AndroidRenderer renderer;

    private boolean               frozen             = false;

    public SpaceGameThread(Context context) {
        super(SpaceData.getInstance());
        SpaceGameState.INSTANCE.setState(GameState.LOADING);

        renderer = new AndroidRenderer();
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

                updatePhysics(elapsed);
                viewport.update(elapsed);

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
            if (spaceData.currentLevel != null) {
                viewport.reset(spaceData.currentLevel.startCenter(), canvasSize);
            } else {
                viewport.reset(viewport.getViewport().center(), canvasSize);
            }
            redrawOnce();
        }
    }

    private void doDraw(Canvas canvas, Rect viewportRect) {
        if (SpaceGameState.INSTANCE.getState().isDoneLoading()) {
            renderer.initialize(canvas, viewportRect, viewport.screenRect);
            spaceData.currentLevel.draw(renderer);
        }
    }

    public float canvasDiagonal() {
        return (float) Math.sqrt(canvasSize.width() * canvasSize.width() + canvasSize.height() * canvasSize.height());
    }
}