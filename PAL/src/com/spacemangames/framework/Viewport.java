package com.spacemangames.framework;

import com.spacemangames.library.SpaceData;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.PALManager;

public class Viewport {
    private static final String TAG                   = Viewport.class.getSimpleName();

    private PointF              flingSpeed;
    private Rect                viewport;
    private boolean             flinging;
    private boolean             focusOnSpaceman;
    private boolean             focusX;
    private boolean             focusY;
    private PointF              previousFocusPoint;
    private boolean             draggingViewport;
    private PointF              viewportDragStart;

    // The screen
    public Rect                 screenRect;

    public static final int     AUTO_FOCUS_MIN_PIXELS = 3;
    public static final float   AUTO_FOCUS_DAMPING    = 1.0f;
    public static final float   FLING_STOP_THRESHOLD  = 10f;
    public static final float   FLING_DAMPING_FACTOR  = 0.9f;

    public Viewport() {
        flingSpeed = new PointF();
        viewport = new Rect();
        viewportDragStart = new PointF();
        previousFocusPoint = new PointF();
        screenRect = new Rect();
    }

    public PointF getFlingSpeed() {
        return flingSpeed;
    }

    public void setFlingSpeed(PointF flingSpeed) {
        this.flingSpeed = flingSpeed;
    }

    public Rect getViewport() {
        return viewport;
    }

    public boolean isFlinging() {
        return flinging;
    }

    public void setFlinging(boolean flinging) {
        this.flinging = flinging;
    }

    public boolean isFocusOnSpaceman() {
        return focusOnSpaceman;
    }

    public void setFocusOnSpaceman(boolean focusOnSpaceman) {
        this.focusOnSpaceman = focusOnSpaceman;
    }

    public void reset(int x, int y, int canvasWidth, int canvasHeight) {
        screenRect.set(0, 0, canvasWidth, canvasHeight);
        synchronized (viewport) {
            viewport.set(x - canvasWidth / 2, y - canvasHeight / 2, x + canvasWidth / 2, y + canvasHeight / 2);
        }
        float scaleWidth = SpaceUtil.BASELINE_WIDTH / canvasWidth;
        float scaleHeight = SpaceUtil.BASELINE_HEIGHT / canvasHeight;
        float scale = Math.max(scaleWidth, scaleHeight);
        if (scale > 1) {
            zoomViewport(scale - 1);
        }

        PALManager.getLog().i(TAG, "viewport: " + viewport.width() + " " + viewport.height());
        setFlinging(false);
    }

    public void resetFocusViewportStatus(boolean on) {
        focusOnSpaceman = on;
        focusX = on;
        focusY = on;
        previousFocusPoint.set(0, 0);
    }

    public void focusOn(PointF position) {
        synchronized (viewport) {
            position.subtract(viewport.center());
            viewport.offset(position);
        }

        setFlinging(false);
    }

    public void viewportFollowSpaceman() {
        PointF spacemanPosition = SpaceData.getInstance().mCurrentLevel.getSpaceManObject().getPosition();

        synchronized (viewport) {
            PointF viewportCenter = viewport.center();

            if (previousFocusPoint.x == 0)
                previousFocusPoint.x = spacemanPosition.x;
            if (previousFocusPoint.y == 0)
                previousFocusPoint.y = spacemanPosition.y;

            if (!focusX) {
                if (previousFocusPoint.x < viewportCenter.x && spacemanPosition.x >= viewportCenter.x
                        || previousFocusPoint.x > viewportCenter.x && spacemanPosition.x < viewportCenter.x) {
                    focusX = true;
                }
            }
            if (!focusY) {
                if (previousFocusPoint.y < viewportCenter.y && spacemanPosition.y > viewportCenter.y
                        || previousFocusPoint.y > viewportCenter.y && spacemanPosition.y < viewportCenter.y) {
                    focusY = true;
                }
            }

            float offsetX = 0;
            float offsetY = 0;

            if (focusX)
                offsetX = Math.round(spacemanPosition.x - viewportCenter.x);
            if (focusY)
                offsetY = Math.round(spacemanPosition.y - viewportCenter.y);

            float offsetXDamped = offsetX * AUTO_FOCUS_DAMPING;
            float offsetYDamped = offsetY * AUTO_FOCUS_DAMPING;

            if (offsetX >= AUTO_FOCUS_MIN_PIXELS && offsetXDamped < AUTO_FOCUS_MIN_PIXELS)
                offsetXDamped = AUTO_FOCUS_MIN_PIXELS;
            if (offsetY >= AUTO_FOCUS_MIN_PIXELS && offsetYDamped < AUTO_FOCUS_MIN_PIXELS)
                offsetYDamped = AUTO_FOCUS_MIN_PIXELS;

            viewport.offset(Math.round(offsetXDamped), Math.round(offsetYDamped));
        }

        previousFocusPoint.set(spacemanPosition);
    }

    public void startViewportDrag(float x, float y) {
        draggingViewport = true;
        viewportDragStart.set(x, y);
    }

    public void dragViewport(float x, float y) {
        if (draggingViewport)
            moveViewport((viewportDragStart.x - x), (viewportDragStart.y - y));

        viewportDragStart.set(x, y);
    }

    public void moveViewport(float x, float y) {
        synchronized (viewport) {
            viewport.offset((int) (x * currentZoom()), (int) (y * currentZoom()));
        }
    }

    public void zoomViewport(float zoom) {
        PALManager.getLog().i(TAG, "zoom: " + zoom);
        synchronized (getViewport()) {
            // don't zoom in further than the max
            if (viewport.width() == screenRect.width() && zoom < 0) {
                return;
            }
            float verticalZoom = zoom * viewport.height();
            float horizontalZoom = zoom * viewport.width();
            viewport.bottom += verticalZoom / 2.0f;
            viewport.top -= verticalZoom / 2.0f;
            viewport.left -= horizontalZoom / 2.0f;
            viewport.right += horizontalZoom / 2.0f;

            // we can't zoom in further than the canvas size...
            if (viewport.width() < screenRect.width() || viewport.height() < screenRect.height()) {
                viewport.right = viewport.left + screenRect.width();
                viewport.bottom = viewport.top + screenRect.height();
            }

            moveViewport(0, 0); // this will check boundaries
        }
    }

    public void stopViewportDrag() {
        draggingViewport = false;
    }

    private float currentZoom() {
        return (float) getViewport().width() / screenRect.width();
    }

    public boolean isValid() {
        if (viewport.width() <= 1)
            return false;

        if (viewport.width() < viewport.height())
            return false;

        return true;
    }
}