package com.spacemangames.library;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.math.PointF;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public class SpaceLevel {
    public static final String    TAG               = SpaceLevel.class.getSimpleName();

    public static final int       ID_LOADING_SCREEN = 0;
    public static final int       ID_HELP1          = 1;
    public static final int       ID_HELP2          = 2;
    public static final int       ID_HELP3          = 3;
    public static final int       ID_HELP4          = 4;

    public ArrayList<SpaceObject> objects;
    public SpaceBackgroundObject  backgroundObject;

    public int                    id;

    public IBitmap                predictionBitmap;

    public String                 name;

    private int                   startY;
    private int                   startX;

    private int                   silver;
    private int                   gold;

    public static Vector2         scratchVector1    = new Vector2();
    public static Vector2         scratchVector2    = new Vector2();
    public static Vector2         scratchVector3    = new Vector2();

    private final Vector2         spaceManSpeedBuf  = new Vector2();

    public SpaceLevel() {
        objects = new ArrayList<SpaceObject>(16);
    }

    public void draw(IRenderer renderer) {
        renderer.doDraw(objects, backgroundObject);
    }

    public void dump() {
        int objCount = objects.size();
        int objIndex = 0;

        PALManager.getLog().i(TAG, "******" + name + "******");
        PALManager.getLog().i(TAG, "Startcenter: " + startX + ":" + startY);
        PALManager.getLog().i(TAG, "Silver: " + silver + " Gold: " + gold);
        PALManager.getLog().i(TAG, "This level contains " + objCount + " objects");
        for (SpaceObject object : objects) {
            PALManager.getLog().i(TAG, "Object: " + objIndex);
            PALManager.getLog().i(TAG, object.toString());
            objIndex++;
        }
    }

    public SpaceManObject getSpaceManObject() {
        int count = objects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject object = objects.get(i);
            if (object.type == ObjectType.SPACEMAN) {
                return (SpaceManObject) object;
            }
        }
        // should not get here! (can get here during loading though!)
        PALManager.getLog().e("SpaceObject", "Could not find spaceman object!");
        return null;
    }

    public void addSpaceMan(PointF position, String bitmap, String arrowBitmap, int collisionSize, IMoveProperties moveProperties) {
        SpaceObject object = new SpaceManObject(bitmap, position, arrowBitmap, collisionSize, moveProperties);
        objects.add(object);
    }

    public void addPlanet(PointF position, String bitmap, boolean lazyLoading, float gravity, int collisionSize, boolean deathOnImpact,
            IMoveProperties moveProperties) {
        SpacePlanetObject object = new SpacePlanetObject(bitmap, lazyLoading, position, gravity, collisionSize, deathOnImpact,
                moveProperties);
        objects.add(object);
    }

    public void addRocket(PointF position, String bitmap, int collisionSize, IMoveProperties moveProperties) {
        SpaceRocketObject object = new SpaceRocketObject(bitmap, position, collisionSize, moveProperties);
        objects.add(object);
    }

    public void addBonus(PointF position, String bitmap, int collisionSize, IMoveProperties moveProperties) {
        SpaceBonusObject object = new SpaceBonusObject(bitmap, position, collisionSize, moveProperties);
        objects.add(object);
    }

    public void setSpaceManSpeed(PointF firePower) {
        spaceManSpeedBuf.set(firePower.x, firePower.y);
        getSpaceManObject().setSpeed(spaceManSpeedBuf);
    }

    public void updatePhysics(float elapsed) {
        if (SpaceGameState.INSTANCE.getState() == GameState.FLYING) {
            updatePhysicsGravity(elapsed);
        }
        updateMovingObjects(elapsed);
    }

    public void reset() {
        int count = objects.size();
        for (int i = 0; i < count; i++) {
            objects.get(i).updateMoving(1);
            objects.get(i).reset();
        }
    }

    private void updateMovingObjects(float elapsed) {
        int count = objects.size();
        for (int i = 0; i < count; i++) {
            objects.get(i).updateMoving(elapsed);
        }
    }

    public void updatePhysicsGravity(float elapsed) {
        int count = objects.size();
        for (int i = 0; i < count; i++) {
            SpaceObject object1 = objects.get(i);
            if (object1.isAffectedByGravity()) {
                for (int j = 0; j < count; j++) {
                    SpaceObject object2 = objects.get(j);
                    updatePhysicsGravity(elapsed, object1, object2);
                }
            }
        }
    }

    private void updatePhysicsGravity(float elapsed, SpaceObject object1, SpaceObject object2) {
        float gravity = object2.gravity();
        if (gravity > 0.f) {
            // Note: we apply some scaling to keep values a bit normal
            scratchVector1.set(object1.body.getPosition());
            scratchVector2.set(object2.body.getPosition());
            float lDistance = scratchVector1.dst(scratchVector2);
            if (lDistance > 0.0) { // if distance == 0, lO1 == lO2
                scratchVector3.set(scratchVector2); // vec3 = vec2
                scratchVector3.sub(scratchVector1); // vec3 = vec2 - vec1
                scratchVector1.set(scratchVector3); // vec1 = vec3
                scratchVector1.mul(1.0f / scratchVector3.len()); // vec1 =
                                                                 // normalized
                                                                 // direction
                // grav pull, inverse square relationship with distance
                float gravityPull = (gravity / (lDistance * lDistance)) * elapsed;
                scratchVector1.mul(gravityPull);
                object1.applyForce(scratchVector1, object1.body.getPosition());
            }
        }
    }

    public void addBackground(String backgroundColorInner, String backgroundColorOuter) {
        backgroundObject = new SpaceBackgroundObject(backgroundColorInner, backgroundColorOuter, name);
    }

    public void setStartCenterY(int startY) {
        this.startY = startY;
    }

    public void setStartCenterX(int startX) {
        this.startX = startX;
    }

    public int startCenterX() {
        return startX;
    }

    public int startCenterY() {
        return startY;
    }

    public PointF startCenter() {
        return new PointF(startX, startY);
    }

    public void setPredictionBitmap(String resource) {
        predictionBitmap = PALManager.getBitmapFactory().createBitmap(resource, false);
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int silver() {
        return silver;
    }

    public int gold() {
        return gold;
    }

    public void releaseLazyMemory() {
        for (SpaceObject object : objects) {
            object.releaseLazyMemory();
        }
    }
}
