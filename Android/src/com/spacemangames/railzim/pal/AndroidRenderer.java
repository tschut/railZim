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
    private boolean          initialized = false;

    private Canvas           canvas;
    private Rect             viewport;
    private Rect             screen;

    private Bitmap           backgroundGradient;
    private Canvas           gradientCanvas;
    private GradientDrawable gradientDrawable;

    private List<Bitmap>     starBitmaps = null;
    private final Canvas     scratchCanvas;

    private int              canvasWidth;
    private int              canvasHeight;

    public AndroidRenderer() {
        scratchCanvas = new Canvas();
    }

    // initialize platform stuff
    public void initialize(Canvas canvas, Rect viewport, Rect screen) {
        this.canvas = canvas;
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        this.viewport = viewport;
        this.screen = screen;
        initialized = true;
    }

    @Override
    public void doDraw(List<SpaceObject> objects, SpaceBackgroundObject backgroundObject) {
        assert !initialized;

        doDraw(backgroundObject);

        int count = objects.size();
        for (int i = 0; i < count; ++i) {
            objects.get(i).dispatchToRenderer(this);
        }

        initialized = false;
    }

    @Override
    public void doDraw(SpaceObject object) {
        AndroidBitmap bitmap = (AndroidBitmap) object.getBitmap();
        Drawable drawable = bitmap.getDrawable();

        float x = SpaceUtil.transformX(viewport, screen, object.position.x);
        float y = SpaceUtil.transformY(viewport, screen, object.position.y);
        float w = SpaceUtil.scaleX(viewport, screen, bitmap.getWidth());
        float h = SpaceUtil.scaleY(viewport, screen, bitmap.getHeight());

        // Draw object
        int yTop = (int) (y - (h / 2.0f));
        int xLeft = (int) (x - (w / 2.0f));
        drawable.setBounds(xLeft, yTop, (int) (xLeft + w), (int) (yTop + h));
        drawable.draw(canvas);
    }

    @Override
    public void doDraw(SpaceBackgroundObject object) {
        // if this returns true we cache the gradient
        if (object.verifyStarFieldReady(canvasWidth, canvasHeight)) {
            SpaceBackgroundObject.GradientProperties gradProps = object.getGradientProperties();
            int gradColor[] = new int[2];
            gradColor[0] = Color.parseColor(gradProps.mInnerColor);
            gradColor[1] = Color.parseColor(gradProps.mOuterColor);
            gradientDrawable = new GradientDrawable(Orientation.TL_BR, gradColor);
            gradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            gradientDrawable.setDither(true);
            gradientDrawable.setGradientCenter(gradProps.mCenterX, gradProps.mCenterY);
            gradientDrawable.setGradientRadius(gradProps.mRadius);
            gradientDrawable.setBounds(0, 0, canvasWidth, canvasHeight);

            backgroundGradient = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.RGB_565);
            gradientCanvas = new Canvas(backgroundGradient);
            gradientDrawable.draw(gradientCanvas);

            // we have to generate the bitmaps for the stars
            List<SpaceBackgroundObject.Star> lStars = object.getStars();
            int lCount = lStars.size();
            if (starBitmaps == null) {
                starBitmaps = new ArrayList<Bitmap>(lCount);
            }
            starBitmaps.clear();
            Paint lPaint = new Paint();
            for (int i = 0; i < lCount; ++i) {
                SpaceBackgroundObject.Star lStar = lStars.get(i);
                int lBitmapSize = (int) lStar.mDiameter;
                if (lBitmapSize < 1) // we can't create bitmaps smaller than 1x1
                    lBitmapSize = 1;
                Bitmap aBitmap = Bitmap.createBitmap(lBitmapSize, lBitmapSize, Bitmap.Config.ARGB_8888);
                scratchCanvas.setBitmap(aBitmap);
                int lColor = Color.HSVToColor(lStar.mColor);
                lPaint.setColor(lColor);
                scratchCanvas.drawCircle(lStar.mRadius, lStar.mRadius, lStar.mRadius, lPaint);
                starBitmaps.add(i, aBitmap);
            }

            // this looks like a good moment for some GC...
            System.gc();
        }

        // first draw background gradient
        canvas.drawBitmap(backgroundGradient, 0, 0, null);

        // now draw all stars
        int lCurrentZoom = viewport.width() / canvasWidth;
        int lXOffset = (screen.centerX() - viewport.centerX()) / lCurrentZoom;
        int lYOffset = (screen.centerY() - viewport.centerY()) / lCurrentZoom;

        // here we assume the stars have been sorted on distance, farthest first
        List<SpaceBackgroundObject.Star> lStars = object.getStars();
        SpaceBackgroundObject.Star lStar;
        float lDepth;
        float lDiam, lX = 0, lY = 0;
        int lCount = lStars.size();
        for (int i = 0; i < lCount; ++i) {
            lStar = lStars.get(i);
            lDiam = lStar.mDiameter;
            lDepth = lStar.mDepth;
            lX = (lXOffset * lDepth - lStar.mX) % (canvasWidth + lDiam);
            lY = (lYOffset * lDepth - lStar.mY) % (canvasHeight + lDiam);

            if (lX + lDiam < 0)
                lX = canvasWidth + (lX + lDiam);
            if (lY + lDiam < 0)
                lY = canvasHeight + (lY + lDiam);

            canvas.drawBitmap(starBitmaps.get(i), lX, lY, null);
        }
    }

    @Override
    public void doDraw(SpaceManObject aObject) {
        doDraw((SpaceObject) aObject);
    }
}
