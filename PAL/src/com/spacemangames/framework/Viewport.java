package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.library.SpaceData;
import com.spacemangames.pal.PALManager;

public class Viewport {
    public static final String TAG                   = "Viewport";

    private Vector2            mFlingSpeed;
    private Rect               mViewport;
    /** Used for flinging */
    private boolean            mFlinging;
    private boolean            mFocusOnSpaceman;
    private boolean            mFocusX;
    private boolean            mFocusY;
    private float              mPrevX;
    private float              mPrevY;
    private boolean            mDraggingViewport;
    private Vector2            mViewportDragStart;

    // The screen
    public Rect                mScreenRect;

    public static final int    AUTO_FOCUS_MIN_PIXELS = 3;
    public static final float  AUTO_FOCUS_DAMPING    = 1.0f;
    public static final float  FLING_STOP_THRESHOLD  = 10f;
    public static final float  FLING_DAMPING_FACTOR  = 0.9f;

    public Viewport(boolean aFocusOnSpaceman, boolean aFocusX, boolean aFocusY, float aPrevX, float aPrevY) {
        mFocusOnSpaceman = aFocusOnSpaceman;
        mFocusX = aFocusX;
        mFocusY = aFocusY;
        mPrevX = aPrevX;
        mPrevY = aPrevY;

        mFlingSpeed = new Vector2(0, 0);
        mViewport = new Rect();
        mViewportDragStart = new Vector2(0, 0);
        mScreenRect = new Rect();
    }

    public Vector2 getFlingSpeed() {
        return mFlingSpeed;
    }

    public void setFlingSpeed(Vector2 aFlingSpeed) {
        mFlingSpeed = aFlingSpeed;
    }

    public Rect getViewport() {
        return mViewport;
    }

    public void setViewport(Rect aViewport) {
        mViewport = aViewport;
    }

    public boolean isFlinging() {
        return mFlinging;
    }

    public void setFlinging(boolean aFlinging) {
        mFlinging = aFlinging;
    }

    public boolean isFocusOnSpaceman() {
        return mFocusOnSpaceman;
    }

    public void setFocusOnSpaceman(boolean aFocusOnSpaceman) {
        mFocusOnSpaceman = aFocusOnSpaceman;
    }

    public boolean isFocusX() {
        return mFocusX;
    }

    public void setFocusX(boolean aFocusX) {
        mFocusX = aFocusX;
    }

    public boolean isFocusY() {
        return mFocusY;
    }

    public void setFocusY(boolean aFocusY) {
        mFocusY = aFocusY;
    }

    public boolean isDraggingViewport() {
        return mDraggingViewport;
    }

    public void setDraggingViewport(boolean aDraggingViewport) {
        mDraggingViewport = aDraggingViewport;
    }

    public void reset(int aX, int aY, int aCanvasWidth, int aCanvasHeight) {
        mScreenRect.set(0, 0, aCanvasWidth, aCanvasHeight);
        synchronized (mViewport) {
            mViewport.set(aX - aCanvasWidth / 2, aY - aCanvasHeight / 2, aX + aCanvasWidth / 2, aY + aCanvasHeight / 2);
        }
        float scaleWidth = SpaceUtil.BASELINE_WIDTH / aCanvasWidth;
        float scaleHeight = SpaceUtil.BASELINE_HEIGHT / aCanvasHeight;
        float scale = Math.max(scaleWidth, scaleHeight);
        PALManager.getLog().i(TAG, "viewport: " + getViewport().width() + " " + getViewport().height());
        PALManager.getLog().i(TAG, "scale: " + scale);
        if (scale > 1) {
            zoomViewport(scale - 1);
        }

        PALManager.getLog().i(TAG, "viewport: " + getViewport().width() + " " + getViewport().height());
        setFlinging(false);
    }

    public void resetFocusViewportStatus(boolean aOn) {
        setFocusOnSpaceman(aOn);
        setFocusX(aOn);
        setFocusY(aOn);
        mPrevX = 0;
        mPrevY = 0;
    }

    public void focusViewportOnSpaceman() {
        SpaceData lData = SpaceData.getInstance();
        float lCurX = lData.mCurrentLevel.getSpaceManObject().mX;
        float lCurY = lData.mCurrentLevel.getSpaceManObject().mY;

        synchronized (getViewport()) {
            float lViewCenterX = getViewport().left + getViewport().width() / 2;
            float lViewCenterY = getViewport().top + getViewport().height() / 2;

            int lOffsetX = Math.round(lCurX - lViewCenterX);
            int lOffsetY = Math.round(lCurY - lViewCenterY);

            mViewport.offset(lOffsetX, lOffsetY);
        }

        setFlinging(false);
    }

    public void viewportFollowSpaceman() {
        SpaceData lData = SpaceData.getInstance();
        float lCurX = lData.mCurrentLevel.getSpaceManObject().mX;
        float lCurY = lData.mCurrentLevel.getSpaceManObject().mY;

        synchronized (mViewport) {
            float lViewCenterX = mViewport.left + mViewport.width() / 2f;
            float lViewCenterY = mViewport.top + mViewport.height() / 2f;

            if (mPrevX == 0)
                mPrevX = lCurX;
            if (mPrevY == 0)
                mPrevY = lCurY;

            if (isFocusX() == false) {
                if (mPrevX < lViewCenterX && lCurX >= lViewCenterX || mPrevX > lViewCenterX && lCurX < lViewCenterX) {
                    setFocusX(true);
                }
            }
            if (isFocusY() == false) {
                if (mPrevY < lViewCenterY && lCurY > lViewCenterY || mPrevY > lViewCenterY && lCurY < lViewCenterY) {
                    setFocusY(true);
                }
            }

            float lOffsetX = 0;
            float lOffsetY = 0;

            if (isFocusX())
                lOffsetX = Math.round(lCurX - lViewCenterX);
            if (isFocusY())
                lOffsetY = Math.round(lCurY - lViewCenterY);

            float lOffsetXDamped = lOffsetX * AUTO_FOCUS_DAMPING;
            float lOffsetYDamped = lOffsetY * AUTO_FOCUS_DAMPING;

            if (lOffsetX >= AUTO_FOCUS_MIN_PIXELS && lOffsetXDamped < AUTO_FOCUS_MIN_PIXELS)
                lOffsetXDamped = AUTO_FOCUS_MIN_PIXELS;
            if (lOffsetY >= AUTO_FOCUS_MIN_PIXELS && lOffsetYDamped < AUTO_FOCUS_MIN_PIXELS)
                lOffsetYDamped = AUTO_FOCUS_MIN_PIXELS;

            mViewport.offset(Math.round(lOffsetXDamped), Math.round(lOffsetYDamped));
        }

        mPrevX = lCurX;
        mPrevY = lCurY;
    }

    public void startViewportDrag(float lX, float lY) {
        setDraggingViewport(true);
        mViewportDragStart.set(lX, lY);
    }

    public void dragViewport(float lX, float lY) {
        if (isDraggingViewport())
            moveViewport((mViewportDragStart.x - lX), (mViewportDragStart.y - lY));

        mViewportDragStart.set(lX, lY);
    }

    public void moveViewport(float aX, float aY) {
        synchronized (mViewport) {
            mViewport.offset((int) (aX * currentZoom()), (int) (aY * currentZoom()));
        }
    }

    public void zoomViewport(float lZoom) {
        PALManager.getLog().i(TAG, "lZoom: " + lZoom);
        synchronized (getViewport()) {
            // don't zoom in further than the max
            if (getViewport().width() == mScreenRect.width() && lZoom < 0) {
                return;
            }
            float lVerticalZoom = lZoom * mViewport.height();
            float lHorizontalZoom = lZoom * mViewport.width();
            mViewport.bottom += lVerticalZoom / 2.0f;
            mViewport.top -= lVerticalZoom / 2.0f;
            mViewport.left -= lHorizontalZoom / 2.0f;
            mViewport.right += lHorizontalZoom / 2.0f;

            // we can't zoom in further than the canvas size...
            if (mViewport.width() < mScreenRect.width() || mViewport.height() < mScreenRect.height()) {
                mViewport.right = mViewport.left + mScreenRect.width();
                mViewport.bottom = mViewport.top + mScreenRect.height();
            }

            moveViewport(0, 0); // this will check boundaries
        }
    }

    public void stopViewportDrag() {
        setDraggingViewport(false);
    }

    public float currentZoom() {
        return (float) getViewport().width() / mScreenRect.width();
    }

    public boolean isValid() {
        if (mViewport.width() <= 1)
            return false;

        if (mViewport.width() < mViewport.height())
            return false;

        return true;
    }

    public void invalidate() {
        synchronized (mViewport) {
            mViewport.set(0, 0, 0, 0);
        }
    }
}