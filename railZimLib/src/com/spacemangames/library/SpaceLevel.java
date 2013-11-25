package com.spacemangames.library;

import java.util.ArrayList;

import com.spacemangames.framework.GameState;
import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.math.PointF;
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

    public String                 name;

    private int                   startY;
    private int                   startX;

    private int                   silver;
    private int                   gold;

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

    public void updatePhysics(float elapsed) {
        if (SpaceGameState.INSTANCE.getState() == GameState.FLYING) {
        }
    }

    public void reset() {
        int count = objects.size();
        for (int i = 0; i < count; i++) {
            objects.get(i).reset();
        }
    }

    public void addBackground() {
        backgroundObject = new SpaceBackgroundObject();
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
