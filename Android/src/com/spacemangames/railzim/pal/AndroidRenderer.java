package com.spacemangames.railzim.pal;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.library.SpaceBackgroundObject;
import com.spacemangames.library.SpaceManObject;
import com.spacemangames.library.SpaceObject;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IRenderer;

public class AndroidRenderer implements IRenderer {
    private boolean          mInitialized = false;

    private Canvas           mCanvas;
    private Rect             mViewport;
    private Rect             mScreen;

    // For the background we make a copy of the gradient bitmap to speed up
    // future rendering
    private Bitmap           mBackgroundGradient;
    private Canvas           mGradientCanvas;
    private GradientDrawable mGradientDrawable;

    // Bitmaps for the stars
    private List<Bitmap>     mStarBitmaps = null;
    private final Canvas     mScratchCanvas;

    // helpers
    private int              mCanvasWidth;
    private int              mCanvasHeight;

    public AndroidRenderer() {
        mScratchCanvas = new Canvas();
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

    @Override
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

    @Override
    public void doDraw(SpaceObject aObject) {
        AndroidBitmap lBitmap = (AndroidBitmap) aObject.getBitmap();
        Drawable lDrawable = lBitmap.getDrawable();

        float lX = SpaceUtil.transformX(mViewport, mScreen, aObject.position.x);
        float lY = SpaceUtil.transformY(mViewport, mScreen, aObject.position.y);
        float lW = SpaceUtil.scaleX(mViewport, mScreen, lBitmap.getWidth());
        float lH = SpaceUtil.scaleY(mViewport, mScreen, lBitmap.getHeight());

        // Draw object
        int yTop = (int) (lY - (lH / 2.0f));
        int xLeft = (int) (lX - (lW / 2.0f));
        lDrawable.setBounds(xLeft, yTop, (int) (xLeft + lW), (int) (yTop + lH));
        lDrawable.draw(mCanvas);
    }

    @Override
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

    @Override
    public void doDraw(SpaceManObject aObject) {
        doDraw((SpaceObject) aObject);
    }
}
