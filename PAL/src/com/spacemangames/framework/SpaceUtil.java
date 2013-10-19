package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.math.Rect;
import com.spacemangames.math.RectF;

public class SpaceUtil {
    private static float SCALE_FOR_RESOLUTION = 1.0f;

    private static RectF SCRATCH_RECTF1      = new RectF();
    private static RectF SCRATCH_RECTF2      = new RectF();

    public static float  DPI;

    public static float  BASELINE_WIDTH      = 800f;
    public static float  BASELINE_HEIGHT     = 480f;

    public static void init(float xDpi, float yDpi) {
        DPI = Math.min(xDpi, yDpi);
    }

    public static void setResolution(Rect resolution) {
        // the baseline is 800x480...
        float scaleX, scaleY;
        scaleX = resolution.width() / BASELINE_WIDTH;
        scaleY = resolution.height() / BASELINE_HEIGHT;
        SCALE_FOR_RESOLUTION = 1f / Math.min(scaleX, scaleY);
    }

    public static float cmToPixels(float cm) {
        float inch = cm * 0.393700787f;
        return inch * DPI;
    }

    public static float pixelsToCm(float pixels) {
        return (pixels / DPI) * (1 / 0.393700787f);
    }

    public static float resolutionScale(float in) {
        return in * SCALE_FOR_RESOLUTION;
    }

    public static int resolutionScale(int in) {
        return (int) (in * SCALE_FOR_RESOLUTION);
    }

    public static float scaleX(Rect in, Rect out, float x) {
        return ((float) out.width() / (float) in.width()) * x;
    }

    public static float scaleY(Rect in, Rect out, float y) {
        return ((float) out.height() / (float) in.height()) * y;
    }

    public static float transformX(RectF in, RectF out, float x) {
        return (out.width() / in.width()) * (x - in.left);
    }

    public static float transformX(Rect in, Rect out, float x) {
        return ((float) out.width() / (float) in.width()) * (x - in.left);
    }

    public static float transformY(RectF in, RectF out, float y) {
        return (out.height() / in.height()) * (y - in.top);
    }

    public static float transformY(Rect in, Rect out, float y) {
        return ((float) out.height() / (float) in.height()) * (y - in.top);
    }

    public static void transformCoordinates(Rect in, Rect out, Vector2 vector) {
        SCRATCH_RECTF1.set(in);
        SCRATCH_RECTF2.set(out);
        transformCoordinates(SCRATCH_RECTF1, SCRATCH_RECTF2, vector);
    }

    public static void transformCoordinates(RectF in, RectF out, Vector2 vector) {
        float lTransformedX = transformX(in, out, vector.x);
        float lTransformedY = transformY(in, out, vector.y);
        vector.set(lTransformedX, lTransformedY);
    }

}
