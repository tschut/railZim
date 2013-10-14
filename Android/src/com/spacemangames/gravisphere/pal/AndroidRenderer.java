package com.spacemangames.gravisphere.pal;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.library.SpaceBackgroundObject;
import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceManObject;
import com.spacemangames.library.SpaceObject;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IRenderer;

public class AndroidRenderer implements IRenderer {
    @SuppressWarnings("unused")
    private static final String         TAG          = "AndroidRenderer";

    private boolean                     mInitialized = false;

    private Canvas                      mCanvas;
    private Rect                        mViewport;
    private Rect                        mScreen;

    // For the background we make a copy of the gradient bitmap to speed up
    // future rendering
    private Bitmap                      mBackgroundGradient;
    private Canvas                      mGradientCanvas;
    private GradientDrawable            mGradientDrawable;

    // Bitmaps for the stars
    private List<Bitmap>                mStarBitmaps = null;
    private final Canvas                mScratchCanvas;

    // helpers
    private int                         mCanvasWidth;
    private int                         mCanvasHeight;
    private final android.graphics.Rect mScratchRect;

    public AndroidRenderer() {
        mScratchCanvas = new Canvas();
        mScratchRect = new android.graphics.Rect();
    }

    // initialize platform stuff
    public void initialize(Canvas aCanvas, Rect aViewport, Rect aScreen) {
        mCanvas = aCanvas;
        mCanvasWidth = mCanvas.getWidth();
        mCanvasHeight = mCanvas.getHeight();

        mViewport = aViewport;
        mScreen = aScreen;
        mInitialized = true;
    }

    public void doDraw(List<SpaceObject> aObjects, SpaceBackgroundObject aBackgroundObject) {
        assert !mInitialized;

        // draw the background first
        doDraw(aBackgroundObject);

        // now draw the rest of the objects
        int lCount = aObjects.size();
        for (int i = 0; i < lCount; ++i) {
            aObjects.get(i).dispatchToRenderer(this);
        }

        mInitialized = false;
    }

    public void doDraw(SpaceObject aObject) {
        AndroidBitmap lBitmap = (AndroidBitmap) aObject.getBitmap();
        Drawable lDrawable = lBitmap.getDrawable();

        float lRotation = (float) (aObject.getBody().getAngle() * (180.0f / Math.PI));

        float lX = SpaceUtil.transformX(mViewport, mScreen, aObject.mX);
        float lY = SpaceUtil.transformY(mViewport, mScreen, aObject.mY);
        float lW = SpaceUtil.scaleX(mViewport, mScreen, lBitmap.getWidth());
        float lH = SpaceUtil.scaleY(mViewport, mScreen, lBitmap.getHeight());

        // Draw object
        int yTop = (int) (lY - (lH / 2.0f));
        int xLeft = (int) (lX - (lW / 2.0f));
        lDrawable.setBounds(xLeft, yTop, (int) (xLeft + lW), (int) (yTop + lH));
        mCanvas.save();
        mCanvas.rotate(lRotation, lX, lY);
        lDrawable.draw(mCanvas);
        mCanvas.restore();
    }

    public void doDraw(SpaceBackgroundObject aObject) {
        // if this returns true we cache the gradient
        if (aObject.verifyStarFieldReady(mCanvasWidth, mCanvasHeight)) {
            SpaceBackgroundObject.GradientProperties gradProps = aObject.getGradientProperties();
            int gradColor[] = new int[2];
            gradColor[0] = Color.parseColor(gradProps.mInnerColor);
            gradColor[1] = Color.parseColor(gradProps.mOuterColor);
            mGradientDrawable = new GradientDrawable(Orientation.TL_BR, gradColor);
            mGradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            mGradientDrawable.setDither(true);
            mGradientDrawable.setGradientCenter(gradProps.mCenterX, gradProps.mCenterY);
            mGradientDrawable.setGradientRadius(gradProps.mRadius);
            mGradientDrawable.setBounds(0, 0, mCanvasWidth, mCanvasHeight);

            mBackgroundGradient = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.RGB_565);
            mGradientCanvas = new Canvas(mBackgroundGradient);
            mGradientDrawable.draw(mGradientCanvas);

            // we have to generate the bitmaps for the stars
            List<SpaceBackgroundObject.Star> lStars = aObject.getStars();
            int lCount = lStars.size();
            if (mStarBitmaps == null) {
                mStarBitmaps = new ArrayList<Bitmap>(lCount);
            }
            mStarBitmaps.clear();
            Paint lPaint = new Paint();
            for (int i = 0; i < lCount; ++i) {
                SpaceBackgroundObject.Star lStar = lStars.get(i);
                int lBitmapSize = (int) lStar.mDiameter;
                if (lBitmapSize < 1) // we can't create bitmaps smaller than 1x1
                    lBitmapSize = 1;
                Bitmap aBitmap = Bitmap.createBitmap(lBitmapSize, lBitmapSize, Bitmap.Config.ARGB_8888);
                mScratchCanvas.setBitmap(aBitmap);
                int lColor = Color.HSVToColor(lStar.mColor);
                lPaint.setColor(lColor);
                mScratchCanvas.drawCircle(lStar.mRadius, lStar.mRadius, lStar.mRadius, lPaint);
                mStarBitmaps.add(i, aBitmap);
            }

            // this looks like a good moment for some GC...
            System.gc();
        }

        // first draw background gradient
        mCanvas.drawBitmap(mBackgroundGradient, 0, 0, null);

        // now draw all stars
        int lCurrentZoom = mViewport.width() / mCanvasWidth;
        int lXOffset = (mScreen.centerX() - mViewport.centerX()) / lCurrentZoom;
        int lYOffset = (mScreen.centerY() - mViewport.centerY()) / lCurrentZoom;

        // here we assume the stars have been sorted on distance, farthest first
        List<SpaceBackgroundObject.Star> lStars = aObject.getStars();
        SpaceBackgroundObject.Star lStar;
        float lDepth;
        float lDiam, lX = 0, lY = 0;
        int lCount = lStars.size();
        for (int i = 0; i < lCount; ++i) {
            lStar = lStars.get(i);
            lDiam = lStar.mDiameter;
            lDepth = lStar.mDepth;
            lX = (lXOffset * lDepth - lStar.mX) % (mCanvasWidth + lDiam);
            lY = (lYOffset * lDepth - lStar.mY) % (mCanvasHeight + lDiam);

            if (lX + lDiam < 0)
                lX = mCanvasWidth + (lX + lDiam);
            if (lY + lDiam < 0)
                lY = mCanvasHeight + (lY + lDiam);

            mCanvas.drawBitmap(mStarBitmaps.get(i), lX, lY, null);
        }
    }

    public void doDraw(SpaceManObject aObject) {
        // do we need to draw the prediction bitmap?
        if (SpaceGameState.getInstance().getState() == SpaceGameState.STATE_CHARGING) {
            AndroidBitmap predictionBitmap = (AndroidBitmap) SpaceData.getInstance().mCurrentLevel.mPredictionBitmap;
            Drawable predictionDrawable = predictionBitmap.getDrawable();

            for (int i = 0; i < aObject.mLastPrediction; ++i) {
                int lX = Math.round(SpaceUtil.transformX(mViewport, mScreen, aObject.mPredictionData.get(i).x));
                int lY = Math.round(SpaceUtil.transformY(mViewport, mScreen, aObject.mPredictionData.get(i).y));
                float lW = SpaceUtil.scaleX(mViewport, mScreen, predictionBitmap.getWidth());
                float lH = SpaceUtil.scaleY(mViewport, mScreen, predictionBitmap.getHeight());

                // Draw object
                int yTop = (int) (lY - (lH / 2.0f));
                int xLeft = (int) (lX - (lW / 2.0f));
                predictionDrawable.setBounds(xLeft, yTop, (int) (xLeft + lW), (int) (yTop + lH));
                predictionDrawable.draw(mCanvas);
            }
            // set spaceman rotation to reflect direction we're going to shoot
            // in
            aObject.setRotation(-1 * SpaceGameState.getInstance().chargingState.getAngle());
        }

        // now use normal drawing function to draw spaceman
        doDraw((SpaceObject) aObject);

        // don't render arrow if we're inside the viewport
        if (Rect.intersects(aObject.getRect(), mViewport))
            return;

        // let the object calculate the arrow position before we render it
        aObject.calculateOutsideArrowPosition(mScreen, mViewport);
        AndroidBitmap lArrowBitmap = (AndroidBitmap) aObject.getArrowBitmap();
        Drawable lArrowDrawable = lArrowBitmap.getDrawable();
        SpaceManObject.ArrowData lArrowData = aObject.getArrowData();
        // Draw arrow
        Rect lR = lArrowData.mRect;
        mScratchRect.set(lR.left, lR.top, lR.right, lR.bottom);
        lArrowDrawable.setAlpha(lArrowData.mAlpha);
        lArrowDrawable.setBounds(mScratchRect);
        mCanvas.save();
        mCanvas.rotate(lArrowData.mAngle, lArrowData.mRect.exactCenterX(), lArrowData.mRect.exactCenterY());
        lArrowDrawable.draw(mCanvas);
        mCanvas.restore();
    }
}
