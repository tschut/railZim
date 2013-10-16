package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;

public class ChargingState {
    public static final int   MAX_CHARGING_POWER  = 150;
    public static final float CHARGING_MULTIPLIER = 1.3f;

    /** Start of charging gesture */
    private float             chargingStartX;
    private float             chargingStartY;

    /** Current pos of charging gesture */
    private float             chargingPower       = 0;
    private float             chargingAngle       = 0;

    private Vector2           spaceManSpeed;

    public ChargingState() {
        spaceManSpeed = new Vector2(0, 0);
    }

    public float chargingPower() {
        return chargingPower;
    }

    public void setChargingStart(float aX, float aY) {
        chargingStartX = aX;
        chargingStartY = aY;
    }

    public void setChargingCurrent(float aX, float aY) {
        float lX = aX - chargingStartX;
        float lY = aY - chargingStartY;
        chargingPower = (float) (Math.sqrt(lX * lX + lY * lY) * CHARGING_MULTIPLIER);
        chargingAngle = (float) Math.atan2(lX, lY);
        // if length is longer than this we have to recalculate x,y
        // coordinates because we're overcharging
        if (chargingPower > MAX_CHARGING_POWER) {
            chargingPower = MAX_CHARGING_POWER;
        }

        // we fire in the opposite direction :)
        lY = -1.0f * (float) Math.cos(chargingAngle) * chargingPower;
        lX = -1.0f * (float) Math.sin(chargingAngle) * chargingPower;
        spaceManSpeed.set(lX, lY);
        // PALManager.getLog().i (TAG, "Speed: " + lX + " " + lY);
    }

    public Vector2 getSpaceManSpeed() {
        return spaceManSpeed;
    }

    public float getAngle() {
        return chargingAngle;
    }

    public void reset() {
        chargingPower = 0;
        chargingAngle = 0;
        spaceManSpeed.set(0, 0);
    }
}