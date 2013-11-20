package com.spacemangames.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.spacemangames.framework.CircularMoveProperties;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.math.PointF;

public class SpaceBackgroundObject extends SpaceObject {
    public class Star implements Comparable<Star> {
        public float   mX, mY;
        public float   mDiameter, mRadius;
        public float   mDepth;               // 0 = no movement, 1 = same as
                                              // viewport
        public float[] mColor = new float[3];

        public Star(float[] aColor, int aX, int aY, float aSize, float aDepth) {
            mX = aX;
            mY = aY;
            mDiameter = aSize;
            mRadius = mDiameter / 2.0f;
            mDepth = aDepth;
            mColor[0] = aColor[0];
            mColor[1] = aColor[1];
            mColor[2] = aColor[2];
        }

        public float getDepth() {
            return mDepth;
        }

        // Comparable
        @Override
        public int compareTo(Star aOther) {
            if (getDepth() < aOther.getDepth()) {
                return -1;
            } else if (getDepth() == aOther.getDepth()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public final class GradientProperties {
        public String mInnerColor;
        public String mOuterColor;
        public float  mCenterX;
        public float  mCenterY;
        public float  mRadius;
    }

    private final static float       STAR_GENERATION_COUNT = 8f;                      // generate
                                                                                       // STAR_GENERATION_COUNT
                                                                                       // stars
                                                                                       // per
                                                                                       // pixel
    private final static float       SIZE_MIN              = 0.02f;                   // in
                                                                                       // cm
    private final static float       SIZE_MAX              = 0.08f;                   // in
                                                                                       // cm
    private final static float       STAR_HUE              = 210f;
    private final static float       STAR_SAT_MIN          = 0.70f;
    private final static float       STAR_SAT_MAX          = 1.0f;
    private final static float       STAR_BRIGHTNESS_MIN   = 0.60f;
    private final static float       STAR_BRIGHTNESS_MAX   = 1.0f;

    private final static int         BACKGROUND_RADIUS_MIN = 600;
    private final static int         BACKGROUND_RADIUS_MAX = 1500;

    private final List<Star>         mStars;
    private final float[]            mScratchColor;

    private final String             mSeed;
    private boolean                  mGenerated            = false;

    private final GradientProperties mGradientProperties   = new GradientProperties();

    public SpaceBackgroundObject(String aBackgroundColorInner, String aBackgroundColorOuter, String aSeed) {
        super(null, false, ObjectType.BACKGROUND, new PointF(), 0, new CircularMoveProperties());

        mSeed = aSeed;

        mStars = new ArrayList<Star>();

        mScratchColor = new float[3]; // hsv

        mGradientProperties.mInnerColor = aBackgroundColorInner;
        mGradientProperties.mOuterColor = aBackgroundColorOuter;
    }

    // Helper function to generate random numbers with distribution 1-x
    // i.e. lots of numbers close to zero, not so much close to one
    @SuppressWarnings("unused")
    private float nextLinearDistFloat(Random aRng) {
        float A, B;
        do {
            A = aRng.nextFloat();
            B = aRng.nextFloat();
            if (B < (1 - A))
                return A;
        } while (true);
    }

    // same as nextLinearDistFloat but with 1-sqrt(x)
    private float nextSqrtDistFloat(Random aRng) {
        float A, B;
        do {
            A = aRng.nextFloat();
            B = aRng.nextFloat();
            if (B < (1 - Math.sqrt(A)))
                return A;
        } while (true);
    }

    // Helper function to get float between two bounds
    private float nextFloat(Random aRng, float aMin, float aMax) {
        return aRng.nextFloat() * (aMax - aMin) + aMin;
    }

    // Helper function to get int between two bounds
    @SuppressWarnings("unused")
    private int nextInt(Random aRng, int aMin, int aMax) {
        return aRng.nextInt(aMax - aMin) + aMin;
    }

    private void GenerateStars(String aSeed, int aMaxX, int aMaxY) {
        mStars.clear();
        int starCount = (int) ((SpaceUtil.pixelsToCm(aMaxX) * SpaceUtil.pixelsToCm(aMaxY)) * STAR_GENERATION_COUNT);
        float maxSize = SpaceUtil.cmToPixels(SIZE_MAX);
        float minSize = SpaceUtil.cmToPixels(SIZE_MIN);
        int backgroundRadiusMin = SpaceUtil.resolutionScale(BACKGROUND_RADIUS_MIN);
        int backgroundRadiusMax = SpaceUtil.resolutionScale(BACKGROUND_RADIUS_MAX);

        // seed the rng
        Random lRng = new Random(aSeed.hashCode());

        // generate stars and add them to the list
        for (int i = 0; i < starCount; ++i) {
            float lDepth = nextSqrtDistFloat(lRng);
            float lSize = lRng.nextFloat() * (maxSize - minSize) + minSize;
            int lX = lRng.nextInt(aMaxX);
            int lY = lRng.nextInt(aMaxY);

            mScratchColor[0] = STAR_HUE;
            mScratchColor[1] = nextFloat(lRng, STAR_SAT_MIN, STAR_SAT_MAX);
            mScratchColor[2] = nextFloat(lRng, STAR_BRIGHTNESS_MIN, STAR_BRIGHTNESS_MAX);

            Star lStar = new Star(mScratchColor, lX, lY, lSize, lDepth);
            mStars.add(lStar);
        }
        // order stars based on distance
        Collections.sort(mStars);

        // generate background gradient properties
        float lRadius = nextFloat(lRng, backgroundRadiusMin, backgroundRadiusMax);
        mGradientProperties.mRadius = lRadius;
        mGradientProperties.mCenterX = lRng.nextFloat();
        mGradientProperties.mCenterY = lRng.nextFloat();

        mGenerated = true;
    }

    public boolean verifyStarFieldReady(int aWidth, int aHeight) {
        // do we need to generate starfield?
        if (!mGenerated) {
            GenerateStars(mSeed, aWidth, aHeight);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        mStars.clear();
        mGenerated = false;
        super.reset();
    }

    public GradientProperties getGradientProperties() {
        return mGradientProperties;
    }

    public List<Star> getStars() {
        return mStars;
    }
}
