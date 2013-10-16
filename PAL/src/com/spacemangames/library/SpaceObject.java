package com.spacemangames.library;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spacemangames.framework.MoveProperties;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public abstract class SpaceObject {
    public static final String MTAG                       = "SpaceObject";

    public static final float  BOX2D_SCALE_FACTOR         = 100f;             // 1
                                                                               // pixel
                                                                               // =
                                                                               // 0.01
                                                                               // meter
                                                                               // =
                                                                               // 1
                                                                               // cm

    public static final float  BOUNCE_NONE                = -1.0f;

    /** Resources */
    private IBitmap            mBitmap;

    // Possible object types
    public static final int    TYPE_BACKGROUND            = 0;
    public static final int    TYPE_SPACEMAN              = 1;
    public static final int    TYPE_PLANET                = 2;
    public static final int    TYPE_ROCKET                = 3;
    public static final int    TYPE_JUNK                  = 4;
    public static final int    TYPE_BONUS                 = 5;

    public static final int    COLLISION_SIZE_IMAGE_WIDTH = -1;

    public int                 mType;

    public float               mStartX;
    public float               mStartY;

    public float               mX;
    public float               mY;

    protected Body             mBody;

    public float               mCollisionSize;

    protected Rect             mRect;

    protected MoveProperties   mMove;
    protected MouseJoint       mMouseJoint;
    protected Body             mMouseJointBody;
    private final Vector2      mMoveScratchVect           = new Vector2(0, 0);

    public SpaceObject(String aBitmap, boolean lazyLoading, int aType, int aX, int aY, int aCollisionSize, MoveProperties aMove) {
        if (aBitmap != null) // objects don't always have a bitmap (e.g.
                             // SpaceBackgroundObject)
            mBitmap = PALManager.getBitmapFactory().createBitmap(aBitmap, lazyLoading);
        mType = aType;
        mStartX = aX;
        mStartY = aY;
        mMove = aMove;

        mRect = new Rect();

        if (aCollisionSize == COLLISION_SIZE_IMAGE_WIDTH) {
            mCollisionSize = mBitmap.getWidth() / 2.0f; // TODO this should use
                                                        // the platform-specific
                                                        // code!
        } else {
            mCollisionSize = aCollisionSize;
        }
    }

    @Override
    public String toString() {
        String lResult = "";
        String lType = getTypeString();

        lResult += "Type:           " + lType + "\n";
        lResult += "X:              " + mStartX + "\n";
        lResult += "Y:              " + mStartY + "\n";
        lResult += "Bitmap:         " + mBitmap.getName() + "\n";

        return lResult;
    }

    public String getTypeString() {
        switch (mType) {
        case TYPE_BACKGROUND:
            return "background";
        case TYPE_SPACEMAN:
            return "spaceman";
        case TYPE_PLANET:
            return "planet";
        case TYPE_ROCKET:
            return "rocket";
        case TYPE_JUNK:
            return "junk";
        case TYPE_BONUS:
            return "bonus";
        default:
            return "Unknown type!";
        }
    }

    // override this in derived class to make an object affected by gravity from
    // others
    public boolean isAffectedByGravity() {
        return false;
    }

    // override this in derived class to make an object exert a gravitational
    // pull
    public float gravity() {
        return -1.f;
    }

    public void setGravity(float parseFloat) {
        // default implementation is empty
    }

    public boolean deathOnImpact() {
        return false;
    }

    public void setDeathOnImpact(boolean aDie) {
        // default implementation is empty
    }

    public float toBox2DCoords(float aIn) {
        return aIn / BOX2D_SCALE_FACTOR;
    }

    public float fromBox2DCoords(float aIn) {
        return aIn * BOX2D_SCALE_FACTOR;
    }

    public Vector2 toBox2DCoords(Vector2 aIn) {
        return aIn.mul(1.0f / BOX2D_SCALE_FACTOR);
    }

    public Vector2 fromBox2DCoords(Vector2 aIn) {
        return aIn.mul(BOX2D_SCALE_FACTOR);
    }

    public void applyForce(Vector2 aForce, Vector2 aPos) {
        mBody.applyForce(toBox2DCoords(aForce), aPos);
    }

    public void reset() {
        mX = mStartX;
        mY = mStartY;
        if (mMove != null) {
            mMove.reset();
        }
        if (mBody != null) {
            synchronized (mBody) {
                World world = mBody.getWorld();
                if (mMouseJoint != null)
                    world.destroyJoint(mMouseJoint);
                world.destroyBody(mBody);
                mBody = createBody(world);
            }
        }
    }

    public void updateMoving(double aElapsed) {
        if (mMouseJoint == null || !mMove.move)
            return;

        mMove.elapse(aElapsed);
        mMoveScratchVect.set(mMove.getPos().x, mMove.getPos().y);
        toBox2DCoords(mMoveScratchVect);

        mMoveScratchVect.add(toBox2DCoords(mStartX), toBox2DCoords(mStartY));

        mMouseJoint.setTarget(mMoveScratchVect);
    }

    public void updatePositions() {
        synchronized (mBody) {
            Vector2 lPosition = fromBox2DCoords(mBody.getPosition());
            mX = lPosition.x;
            mY = lPosition.y;
        }
    }

    public void dispatchToRenderer(IRenderer aRenderer) {
        aRenderer.doDraw(this);
    }

    public void setSpeed(Vector2 aSpeed) {
        aSpeed = toBox2DCoords(aSpeed);
        // Log.i (MTAG, "Setting speed to " + aSpeed.x + " " + aSpeed.y);
        mBody.applyForce(aSpeed, mBody.getWorldCenter());
    }

    public Rect getRect() {
        float lHalfWidth = mBitmap.getWidth() / 2.0f;
        float lHalfHeight = mBitmap.getHeight() / 2.0f;
        mRect.set((int) (mX - lHalfWidth), (int) (mY - lHalfHeight), (int) (mX + lHalfWidth), (int) (mY + lHalfHeight));
        return mRect;
    }

    public Body createBody(World world) {
        Shape sd = createShape();
        FixtureDef fdef = createFixtureDef(sd);
        BodyDef bd = createBodyDef();
        mBody = createBody(world, bd, fdef);
        setupMouseJoint(world, bd);

        updatePositions();

        return mBody;
    }

    public void setupMouseJoint(World world, BodyDef bd) {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0, 0);
        mMouseJointBody = world.createBody(groundBodyDef);
        // create a mousejoint
        MouseJointDef mouseJointDef = new MouseJointDef();
        mouseJointDef.bodyA = mMouseJointBody;
        mouseJointDef.bodyB = mBody;
        mouseJointDef.dampingRatio = 0.2f;
        mouseJointDef.frequencyHz = 30;
        mouseJointDef.maxForce = 20000.0f;
        mouseJointDef.collideConnected = true;
        mouseJointDef.target.set(bd.position);
        mMouseJoint = (MouseJoint) world.createJoint(mouseJointDef);
    }

    public Body createBody(World world, BodyDef bd, FixtureDef fdef) {
        mBody = world.createBody(bd);
        mBody.createFixture(fdef);
        mBody.setUserData(this);
        mBody.setType(BodyDef.BodyType.DynamicBody);

        return mBody;
    }

    public FixtureDef createFixtureDef(Shape sd) {
        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 0.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.0f;

        return fdef;
    }

    public Shape createShape() {
        CircleShape sd = new CircleShape();
        sd.setRadius(toBox2DCoords(mCollisionSize));

        return sd;
    }

    public BodyDef createBodyDef() {
        BodyDef bd = new BodyDef();
        bd.allowSleep = false;
        bd.position.set(getStartX(), getStartY());
        bd.angle = 0;

        return bd;
    }

    public float getStartX() {
        return toBox2DCoords(mX + mMove.getPos().x);
    }

    public float getStartY() {
        return toBox2DCoords(mY + mMove.getPos().y);
    }

    public Body getBody() {
        return mBody;
    }

    public IBitmap getBitmap() {
        return mBitmap;
    }

    public void offset(int aX, int aY) {
        mStartX = mStartX + aX;
        mStartY = mStartY + aY;
        reset();
    }

    public MoveProperties getMoveProperties() {
        return mMove;
    }

    public int getCollisionSize() {
        if (mCollisionSize == mBitmap.getWidth() / 2.0f)
            return -1;
        else
            return (int) mCollisionSize;
    }

    public void setBitmap(IBitmap aBitmap) {
        mBitmap = aBitmap;
    }

    public void releaseLazyMemory() {
        mBitmap.releaseLazyMemory();
    }
}
