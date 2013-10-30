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
    private Boolean             focusX                = new Boolean(false);
    private Boolean             focusY                = new Boolean(false);
    private PointF              previousFocusPoint;

    public Rect                 screenRect;

    private static final int    AUTO_FOCUS_MIN_PIXELS = 3;
    private static final float  AUTO_FOCUS_DAMPING    = 1.0f;
    private static final float  FLING_STOP_THRESHOLD  = 10f;
    private static final float  FLING_DAMPING_FACTOR  = 0.9f;

    public Viewport() {
        viewport = new Rect();
        previousFocusPoint = new PointF();
        screenRect = new Rect();
    }

    public void setFlinging(PointF flingSpeed) {
        this.flingSpeed = flingSpeed;
        flinging = true;
    }

    public Rect getViewport() {
        return viewport;
    }

    public boolean isFocusOnSpaceman() {
        return focusOnSpaceman;
    }

    public void setFocusOnSpaceman(boolean focusOnSpaceman) {
        this.focusOnSpaceman = focusOnSpaceman;
    }

    public void reset(PointF center, Rect canvasSize) {
        screenRect.set(canvasSize);
        viewport.set(canvasSize);
        viewport.offset((int) -center.x / 2, (int) -center.y / 2);
        float scaleWidth = SpaceUtil.BASELINE_WIDTH / canvasSize.width();
        float scaleHeight = SpaceUtil.BASELINE_HEIGHT / canvasSize.height();
        float scale = Math.max(scaleWidth, scaleHeight);
        if (scale > 1) {
            zoomViewport(scale - 1);
        }

        PALManager.getLog().i(TAG, "viewport: " + viewport.width() + " " + viewport.height());
        flinging = false;
    }

    public void resetFocusViewportStatus(boolean focusOnSpaceman) {
        this.focusOnSpaceman = focusOnSpaceman;
        focusX = focusOnSpaceman;
        focusY = focusOnSpaceman;
        previousFocusPoint.set(0, 0);
    }

    public void focusOn(PointF position) {
        position.subtract(viewport.center());
        viewport.offset(position);
        flinging = false;
    }

    public void viewportFollowSpaceman() {
        PointF spacemanPosition = SpaceData.getInstance().currentLevel.getSpaceManObject().getPosition();

        PointF viewportCenter = viewport.center();

        initializePreviousFocusPosition(spacemanPosition);
        float offsetXDamped = calculateOffsetToFollowSpaceman(focusX, previousFocusPoint.x, spacemanPosition.x, viewportCenter.x);
        float offsetYDamped = calculateOffsetToFollowSpaceman(focusY, previousFocusPoint.y, spacemanPosition.y, viewportCenter.y);

        viewport.offset(Math.round(offsetXDamped), Math.round(offsetYDamped));

        previousFocusPoint.set(spacemanPosition);
    }

    private float calculateOffsetToFollowSpaceman(Boolean focus, float previousFocusPoint, float spacemanPosition, float viewportCenter) {
        float result = 0;
        if (!focus) {
            focus = (previousFocusPoint < viewportCenter && spacemanPosition >= viewportCenter || previousFocusPoint > viewportCenter
                    && spacemanPosition < viewportCenter);
        }

        float offset = 0;
        if (focus) {
            offset = Math.round(spacemanPosition - viewportCenter);

            result = offset * AUTO_FOCUS_DAMPING;
            if (offset >= AUTO_FOCUS_MIN_PIXELS && result < AUTO_FOCUS_MIN_PIXELS) {
                result = AUTO_FOCUS_MIN_PIXELS;
            }
        }

        return result;
    }

    private void initializePreviousFocusPosition(PointF spacemanPosition) {
        if (previousFocusPoint.x == 0)
            previousFocusPoint.x = spacemanPosition.x;
        if (previousFocusPoint.y == 0)
            previousFocusPoint.y = spacemanPosition.y;
    }

    public void moveViewport(float x, float y) {
        viewport.offset((int) (x * currentZoom()), (int) (y * currentZoom()));
    }

    public void zoomViewport(float zoom) {
        if (viewport.width() <= screenRect.width() && zoom < 0) {
            return;
        }

        viewport.scale(zoom);

        if (viewportAboveMaximumZoomLevel()) {
            viewport.right = viewport.left + screenRect.width();
            viewport.bottom = viewport.top + screenRect.height();
        }

        moveViewport(0, 0);
    }

    private boolean viewportAboveMaximumZoomLevel() {
        return viewport.width() < screenRect.width() || viewport.height() < screenRect.height();
    }

    private float currentZoom() {
        return (float) viewport.width() / screenRect.width();
    }

    public boolean isValid() {
        if (viewport.width() <= 1)
            return false;

        if (viewport.width() < viewport.height())
            return false;

        return true;
    }

    public PointF toScreenCoordinates(PointF spacemanPosition) {
        return SpaceUtil.transform(viewport, screenRect, spacemanPosition);
    }

    public void update(float elapsed) {
        if (focusOnSpaceman) {
            viewportFollowSpaceman();
        }

        if (flinging) {
            moveViewport(flingSpeed.x * elapsed, flingSpeed.y * elapsed);
            flingSpeed.multiply(FLING_DAMPING_FACTOR);
            if (flingSpeed.length() < FLING_STOP_THRESHOLD) {
                flinging = false;
            }
        }
    }
}