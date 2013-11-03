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
import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public abstract class SpaceObject {
    private static final float BOX2D_SCALE_FACTOR         = 100f;

    /** Resources */
    private IBitmap            bitmap;

    // Possible object types
    public static final int    TYPE_BACKGROUND            = 0;
    public static final int    TYPE_SPACEMAN              = 1;
    public static final int    TYPE_PLANET                = 2;
    public static final int    TYPE_ROCKET                = 3;
    public static final int    TYPE_JUNK                  = 4;
    public static final int    TYPE_BONUS                 = 5;

    private static final int   COLLISION_SIZE_IMAGE_WIDTH = -1;

    public int                 type;

    private float              startX;
    private float              startY;

    public float               x;
    public float               y;

    protected Body             body;

    private float              collisionSize;

    private Rect               rect;

    protected IMoveProperties  move;
    private MouseJoint         mouseJoint;
    private Body               mouseJointBody;
    private final Vector2      moveScratchVect            = new Vector2(0, 0);

    public SpaceObject(String bitmap, boolean lazyLoading, int type, int x, int y, int collisionSize, IMoveProperties moveProperties) {
        if (bitmap != null) {
            this.bitmap = PALManager.getBitmapFactory().createBitmap(bitmap, lazyLoading);
        }
        this.type = type;
        this.startX = x;
        this.startY = y;
        move = moveProperties;

        rect = new Rect();

        if (collisionSize == COLLISION_SIZE_IMAGE_WIDTH) {
            this.collisionSize = this.bitmap.getWidth() / 2.0f;
        } else {
            this.collisionSize = collisionSize;
        }
    }

    @Override
    public String toString() {
        String lResult = "";
        String lType = getTypeString();

        lResult += "Type:           " + lType + "\n";
        lResult += "X:              " + startX + "\n";
        lResult += "Y:              " + startY + "\n";
        lResult += "Bitmap:         " + bitmap.getName() + "\n";

        return lResult;
    }

    private String getTypeString() {
        switch (type) {
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

    public boolean isAffectedByGravity() {
        return false;
    }

    public float gravity() {
        return -1.f;
    }

    public boolean deathOnImpact() {
        return false;
    }

    private float toBox2DCoords(float in) {
        return in / BOX2D_SCALE_FACTOR;
    }

    private Vector2 toBox2DCoords(Vector2 in) {
        return in.mul(1.0f / BOX2D_SCALE_FACTOR);
    }

    private Vector2 fromBox2DCoords(Vector2 in) {
        return in.mul(BOX2D_SCALE_FACTOR);
    }

    public void applyForce(Vector2 force, Vector2 pos) {
        body.applyForce(toBox2DCoords(force), pos);
    }

    public void reset() {
        x = startX;
        y = startY;
        if (move != null) {
            move.reset();
        }
        if (body != null) {
            synchronized (body) {
                World world = body.getWorld();
                if (mouseJoint != null) {
                    world.destroyJoint(mouseJoint);
                }
                world.destroyBody(body);
                body = createBody(world);
            }
        }
    }

    public void updateMoving(double elapsed) {
        if (mouseJoint != null) {
            move.elapse(elapsed);
            moveScratchVect.set(move.getPos().x, move.getPos().y);
            toBox2DCoords(moveScratchVect);

            moveScratchVect.add(toBox2DCoords(startX), toBox2DCoords(startY));

            mouseJoint.setTarget(moveScratchVect);
        }
    }

    public void updatePositions() {
        synchronized (body) {
            Vector2 position = fromBox2DCoords(body.getPosition());
            x = position.x;
            y = position.y;
        }
    }

    public void dispatchToRenderer(IRenderer renderer) {
        renderer.doDraw(this);
    }

    public void setSpeed(Vector2 speed) {
        speed = toBox2DCoords(speed);
        body.applyForce(speed, body.getWorldCenter());
    }

    public Rect getRect() {
        float halfWidth = bitmap.getWidth() / 2.0f;
        float halfHeight = bitmap.getHeight() / 2.0f;
        rect.set((int) (x - halfWidth), (int) (y - halfHeight), (int) (x + halfWidth), (int) (y + halfHeight));
        return rect;
    }

    public Body createBody(World world) {
        Shape sd = createShape();
        FixtureDef fdef = createFixtureDef(sd);
        BodyDef bd = createBodyDef();
        body = createBody(world, bd, fdef);
        setupMouseJoint(world, bd);

        updatePositions();

        return body;
    }

    protected void setupMouseJoint(World world, BodyDef bd) {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0, 0);
        mouseJointBody = world.createBody(groundBodyDef);
        MouseJointDef mouseJointDef = new MouseJointDef();
        mouseJointDef.bodyA = mouseJointBody;
        mouseJointDef.bodyB = body;
        mouseJointDef.dampingRatio = 0.2f;
        mouseJointDef.frequencyHz = 30;
        mouseJointDef.maxForce = 20000.0f;
        mouseJointDef.collideConnected = true;
        mouseJointDef.target.set(bd.position);
        mouseJoint = (MouseJoint) world.createJoint(mouseJointDef);
    }

    private Body createBody(World world, BodyDef bd, FixtureDef fdef) {
        body = world.createBody(bd);
        body.createFixture(fdef);
        body.setUserData(this);
        body.setType(BodyDef.BodyType.DynamicBody);

        return body;
    }

    protected FixtureDef createFixtureDef(Shape sd) {
        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 0.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.0f;

        return fdef;
    }

    private Shape createShape() {
        CircleShape sd = new CircleShape();
        sd.setRadius(toBox2DCoords(collisionSize));

        return sd;
    }

    private BodyDef createBodyDef() {
        BodyDef bd = new BodyDef();
        bd.allowSleep = false;
        bd.position.set(getStartX(), getStartY());
        bd.angle = 0;

        return bd;
    }

    private float getStartX() {
        return toBox2DCoords(x + move.getPos().x);
    }

    private float getStartY() {
        return toBox2DCoords(y + move.getPos().y);
    }

    public Body getBody() {
        return body;
    }

    public IBitmap getBitmap() {
        return bitmap;
    }

    public void releaseLazyMemory() {
        bitmap.releaseLazyMemory();
    }

    public PointF getPosition() {
        return new PointF(x, y);
    }
}
