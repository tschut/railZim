package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.math.Rect;
import com.spacemangames.math.RectF;

public class SpaceUtil {
    private static float mScaleForResolution = 1.0f;

    private static RectF mScratchRectF1      = new RectF();
    private static RectF mScratchRectF2      = new RectF();

    public static float  mDPI;

    public static float  BASELINE_WIDTH      = 800f;
    public static float  BASELINE_HEIGHT     = 480f;

    public static void init(float xDpi, float yDpi) {
        mDPI = Math.min(xDpi, yDpi);
    }

    public static void setResolution(Rect aRes) {
        // the baseline is 800x480...
        float scaleX, scaleY;
        scaleX = aRes.width() / BASELINE_WIDTH;
        scaleY = aRes.height() / BASELINE_HEIGHT;
        mScaleForResolution = 1f / Math.min(scaleX, scaleY);
    }

    public static float cmToPixels(float cm) {
        float inch = cm * 0.393700787f;
        return inch * mDPI;
    }

    public static float pixelsToCm(float pixels) {
        return (pixels / mDPI) * (1 / 0.393700787f);
    }

    public static float resolutionScale(float in) {
        return in * mScaleForResolution;
    }

    public static int resolutionScale(int in) {
        return (int) (in * mScaleForResolution);
    }

    public static float scaleX(Rect in, Rect out, float aX) {
        return ((float) out.width() / (float) in.width()) * aX;
    }

    public static float scaleY(Rect in, Rect out, float aY) {
        return ((float) out.height() / (float) in.height()) * aY;
    }

    public static float transformX(RectF in, RectF out, float aX) {
        return (out.width() / in.width()) * (aX - in.left);
    }

    public static float transformX(Rect in, Rect out, float aX) {
        return ((float) out.width() / (float) in.width()) * (aX - in.left);
    }

    public static float transformY(RectF in, RectF out, float aY) {
        return (out.height() / in.height()) * (aY - in.top);
    }

    public static float transformY(Rect in, Rect out, float aY) {
        return ((float) out.height() / (float) in.height()) * (aY - in.top);
    }

    public static void transformCoordinates(Rect in, Rect out, Vector2 vec) {
        mScratchRectF1.set(in);
        mScratchRectF2.set(out);
        transformCoordinates(mScratchRectF1, mScratchRectF2, vec);
    }

    public static void transformCoordinates(RectF in, RectF out, Vector2 vec) {
        float lTransformedX = transformX(in, out, vec.x);
        float lTransformedY = transformY(in, out, vec.y);
        vec.set(lTransformedX, lTransformedY);
    }

}
