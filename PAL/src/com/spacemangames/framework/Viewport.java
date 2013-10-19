package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.library.SpaceData;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.PALManager;

public class Viewport {
    private static final String TAG                   = Viewport.class.getSimpleName();

    private Vector2             flingSpeed;
    private Rect                viewport;
    private boolean             flinging;
    private boolean             focusOnSpaceman;
    private boolean             focusX;
    private boolean             focusY;
    private float               prevX;
    private float               prevY;
    private boolean             draggingViewport;
    private Vector2             viewportDragStart;

    // The screen
    public Rect                 screenRect;

    public static final int     AUTO_FOCUS_MIN_PIXELS = 3;
    public static final float   AUTO_FOCUS_DAMPING    = 1.0f;
    public static final float   FLING_STOP_THRESHOLD  = 10f;
    public static final float   FLING_DAMPING_FACTOR  = 0.9f;

    public Viewport(boolean focusOnSpaceman, boolean focusX, boolean focusY, float prevX, float prevY) {
        this.focusOnSpaceman = focusOnSpaceman;
        this.focusX = focusX;
        this.focusY = focusY;
        this.prevX = prevX;
        this.prevY = prevY;

        flingSpeed = new Vector2(0, 0);
        viewport = new Rect();
        viewportDragStart = new Vector2(0, 0);
        screenRect = new Rect();
    }

    public Vector2 getFlingSpeed() {
        return flingSpeed;
    }

    public void setFlingSpeed(Vector2 flingSpeed) {
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
        PALManager.getLog().i(TAG, "scale: " + scale);
        if (scale > 1) {
            zoomViewport(scale - 1);
        }

        PALManager.getLog().i(TAG, "viewport: " + getViewport().width() + " " + getViewport().height());
        setFlinging(false);
    }

    public void resetFocusViewportStatus(boolean on) {
        setFocusOnSpaceman(on);
        focusX = on;
        focusY = on;
        prevX = 0;
        prevY = 0;
    }

    public void focusViewportOnSpaceman() {
        SpaceData data = SpaceData.getInstance();
        float curX = data.mCurrentLevel.getSpaceManObject().mX;
        float curY = data.mCurrentLevel.getSpaceManObject().mY;

        synchronized (getViewport()) {
            float viewCenterX = getViewport().left + getViewport().width() / 2;
            float viewCenterY = getViewport().top + getViewport().height() / 2;

            int offsetX = Math.round(curX - viewCenterX);
            int offsetY = Math.round(curY - viewCenterY);

            viewport.offset(offsetX, offsetY);
        }

        setFlinging(false);
    }

    public void viewportFollowSpaceman() {
        SpaceData spaceData = SpaceData.getInstance();
        float curX = spaceData.mCurrentLevel.getSpaceManObject().mX;
        float curY = spaceData.mCurrentLevel.getSpaceManObject().mY;

        synchronized (viewport) {
            float viewCenterX = viewport.left + viewport.width() / 2f;
            float viewCenterY = viewport.top + viewport.height() / 2f;

            if (prevX == 0)
                prevX = curX;
            if (prevY == 0)
                prevY = curY;

            if (focusX == false) {
                if (prevX < viewCenterX && curX >= viewCenterX || prevX > viewCenterX && curX < viewCenterX) {
                    focusX = true;
                }
            }
            if (focusY == false) {
                if (prevY < viewCenterY && curY > viewCenterY || prevY > viewCenterY && curY < viewCenterY) {
                    focusY = true;
                }
            }

            float offsetX = 0;
            float offsetY = 0;

            if (focusX)
                offsetX = Math.round(curX - viewCenterX);
            if (focusY)
                offsetY = Math.round(curY - viewCenterY);

            float offsetXDamped = offsetX * AUTO_FOCUS_DAMPING;
            float offsetYDamped = offsetY * AUTO_FOCUS_DAMPING;

            if (offsetX >= AUTO_FOCUS_MIN_PIXELS && offsetXDamped < AUTO_FOCUS_MIN_PIXELS)
                offsetXDamped = AUTO_FOCUS_MIN_PIXELS;
            if (offsetY >= AUTO_FOCUS_MIN_PIXELS && offsetYDamped < AUTO_FOCUS_MIN_PIXELS)
                offsetYDamped = AUTO_FOCUS_MIN_PIXELS;

            viewport.offset(Math.round(offsetXDamped), Math.round(offsetYDamped));
        }

        prevX = curX;
        prevY = curY;
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