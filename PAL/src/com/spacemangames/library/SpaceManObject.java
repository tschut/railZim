package com.spacemangames.library;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.spacemangames.framework.MoveProperties;
import com.spacemangames.framework.Rect;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.math.PointF;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public class SpaceManObject extends SpaceObject {
    public class ArrowData {
        public float mAngle;
        public int mAlpha;
        public Rect mRect;

        public ArrowData() {
            mAngle = 0f;
            mAlpha = 0;
            mRect = new Rect(0, 0, 0, 0);
        }
    }

    /** Variables for drawing the arrow when spaceman is offscreen */
    private final IBitmap mArrowBitmap;
    private final ArrowData mArrowData;
    private final Rect mScratchRect;

    private final Vector2 mScratchVector;

    /** Prediction of where spaceman is going */
    public ArrayList<PointF> mPredictionData;
    public int mLastPrediction;

    public SpaceManObject(String aBitmap, int aX, int aY, String aArrowResource, int aCollisionSize, MoveProperties aMoveProperties) {
        super(aBitmap, false, TYPE_SPACEMAN, aX, aY, aCollisionSize, aMoveProperties);

        mArrowBitmap = PALManager.getBitmapFactory().createBitmap(aArrowResource, false);
        mArrowData = new ArrowData();

        mScratchRect = new Rect();
        mScratchVector = new Vector2(0, 0);

        mPredictionData = new ArrayList<PointF>();
        int predictionPoints = (int) ((SpaceData.PREDICT_SECONDS / SpaceData.PREDICTION_STEP) + 1); // + 1 just to be sure we've got
                                                                                                    // enough
        for (int i = 0; i < predictionPoints; ++i) {
            mPredictionData.add(new PointF(0, 0));
        }
    }

    @Override
    public void dispatchToRenderer(IRenderer aRenderer) {
        aRenderer.doDraw(this);
    }

    @Override
    public boolean isAffectedByGravity() {
        return true;
    }

    public void calculateOutsideArrowPosition(Rect aScreen, Rect aViewport) {
        // local copies for efficiency
        int lArrowWidth = mArrowBitmap.getWidth();
        int lArrowHeight = mArrowBitmap.getHeight();

        mScratchRect.set(aScreen);

        float lX = SpaceUtil.transformX(aViewport, aScreen, mX);
        float lY = SpaceUtil.transformY(aViewport, aScreen, mY);

        // these are in canvas coordinates!
        float lArrowCenterX = 0, lArrowCenterY = 0;
        float lAngle = 0;

        // easy case 1, left or right
        if (lY > mScratchRect.top + lArrowHeight / 2 && lY < mScratchRect.bottom - lArrowHeight / 2) {
            lArrowCenterY = lY;
            if (lX < mScratchRect.centerX()) { // left
                lAngle = 180;
                lArrowCenterX = mScratchRect.left + lArrowWidth / 2;
            } else { // right
                lAngle = 0;
                lArrowCenterX = mScratchRect.right - lArrowWidth / 2;
            }
        }
        // easy case 2, top or bottom
        else if (lX > mScratchRect.left + lArrowWidth / 2 && lX < mScratchRect.right - lArrowWidth / 2) {
            lArrowCenterX = lX;
            if (lY < mScratchRect.centerY()) { // top
                lAngle = 270;
                lArrowCenterY = mScratchRect.top + lArrowHeight / 2;
            } else { // bottom
                lAngle = 90;
                lArrowCenterY = mScratchRect.bottom - lArrowHeight / 2;
            }
        }
        // must be one of the corners...
        else {
            if (lX < mScratchRect.centerX() && lY < mScratchRect.centerY()) {
                // top left
                lArrowCenterX = mScratchRect.left + lArrowWidth / 2;
                lArrowCenterY = mScratchRect.top + lArrowHeight / 2;
            } else if (lX < mScratchRect.centerX() && lY > mScratchRect.centerY()) {
                // bottom left
                lArrowCenterX = mScratchRect.left + lArrowWidth / 2;
                lArrowCenterY = mScratchRect.bottom - lArrowHeight / 2;
            } else if (lX > mScratchRect.centerX() && lY < mScratchRect.centerY()) {
                // top right
                lArrowCenterX = mScratchRect.right - lArrowWidth / 2;
                lArrowCenterY = mScratchRect.top + lArrowHeight / 2;
            } else {
                // bottom right
                lArrowCenterX = mScratchRect.right - lArrowWidth / 2;
                lArrowCenterY = mScratchRect.bottom - lArrowHeight / 2;
            }
            // calculate angle
            mScratchVector.set(lX - lArrowCenterX, lY - lArrowCenterY);
            lAngle = mScratchVector.angle();
        }

        // calculate distance
        mScratchVector.set(lX - lArrowCenterX, lY - lArrowCenterY);
        float lDist = mScratchVector.len();
        int lAlpha = (int) ((lDist / 2));
        if (lAlpha < 10)
            lAlpha = 10;
        if (lAlpha > 255)
            lAlpha = 255;

        mArrowData.mAngle = lAngle;
        mArrowData.mAlpha = lAlpha;
        mArrowData.mRect.set((int) lArrowCenterX - lArrowWidth / 2, (int) lArrowCenterY - lArrowHeight / 2, (int) lArrowCenterX
                + lArrowWidth / 2, (int) lArrowCenterY + lArrowHeight / 2);
    }

    @Override
    public void setupMouseJoint(World world, BodyDef bd) {
        if (mMove.mMove)
            super.setupMouseJoint(world, bd);
    }

    @Override
    public FixtureDef createFixtureDef(Shape sd) {
        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 0.05f;
        fdef.friction = 1.0f;
        fdef.restitution = 0.95f;

        return fdef;
    }

    public void clearPredictionPoints() {
        mLastPrediction = 0;
    }

    public void setPredictionPoint(int predictionIndex) {
        mPredictionData.get(predictionIndex).set(mX, mY);
        mLastPrediction++;
    }

    public void setRotation(float aAngle) {
        mBody.setTransform(mBody.getPosition(), aAngle);
    }

    public ArrowData getArrowData() {
        return mArrowData;
    }

    public IBitmap getArrowBitmap() {
        return mArrowBitmap;
    }
}
