package com.spacemangames.framework;

import com.spacemangames.math.Rect;

public class SpaceUtil {
    private static float SCALE_FOR_RESOLUTION = 1.0f;

    private static float DPI;

    public static float  BASELINE_WIDTH       = 800f;
    public static float  BASELINE_HEIGHT      = 480f;

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

    public static float transformX(Rect in, Rect out, float x) {
        return ((float) out.width() / (float) in.width()) * (x - in.left);
    }

    public static float transformY(Rect in, Rect out, float y) {
        return ((float) out.height() / (float) in.height()) * (y - in.top);
    }
}
