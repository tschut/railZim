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

    private IBitmap            bitmap;

    private static final int   COLLISION_SIZE_IMAGE_WIDTH = -1;

    public ObjectType          type;

    private PointF             startPosition              = new PointF();
    public PointF              position                   = new PointF();

    protected Body             body;

    private float              collisionSize;

    private Rect               rect;

    protected IMoveProperties  move;
    private MouseJoint         mouseJoint;
    private Body               mouseJointBody;
    private final Vector2      moveScratchVect            = new Vector2(0, 0);

    public SpaceObject(String bitmap, boolean lazyLoading, ObjectType type, PointF startPosition, int collisionSize,
            IMoveProperties moveProperties) {
        if (bitmap != null) {
            this.bitmap = PALManager.getBitmapFactory().createBitmap(bitmap, lazyLoading);
        }
        this.type = type;
        this.startPosition.set(startPosition);
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
        StringBuilder builder = new StringBuilder();

        builder.append("Type:           " + type.toString() + "\n");
        builder.append("X:              " + startPosition.x + "\n");
        builder.append("Y:              " + startPosition.y + "\n");
        builder.append("Bitmap:         " + bitmap.getName() + "\n");

        return builder.toString();
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

    private PointF toBox2DCoords(PointF in) {
        in.multiply(1.0f / BOX2D_SCALE_FACTOR);
        return in;
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
        position.set(startPosition);
        move.reset();
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

            moveScratchVect.add(toBox2DCoords(startPosition.x), toBox2DCoords(startPosition.y));

            mouseJoint.setTarget(moveScratchVect);
        }
    }

    public void updatePositions() {
        synchronized (body) {
            Vector2 box2dPosition = fromBox2DCoords(body.getPosition());
            position.set(box2dPosition.x, box2dPosition.y);
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
        rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        rect.offset(position);
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
        PointF position = getStart();
        bd.position.set(new Vector2(position.x, position.y));
        bd.angle = 0;

        return bd;
    }

    private PointF getStart() {
        PointF result = new PointF(position);
        result.add(move.getPos());
        return toBox2DCoords(result);
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
        return position;
    }
}
