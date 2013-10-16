package com.spacemangames.framework;

import com.spacemangames.math.PointF;

public class ChargingState {
    private static final int   MAX_CHARGING_POWER  = 150;
    private static final float CHARGING_MULTIPLIER = 1.3f;

    private float              chargingStartX;
    private float              chargingStartY;

    private float              chargingPower       = 0;
    private float              chargingAngle       = 0;

    public float chargingPower() {
        return chargingPower;
    }

    public void setChargingStart(float aX, float aY) {
        chargingStartX = aX;
        chargingStartY = aY;
    }

    public void setChargingCurrent(float x, float y) {
        x = x - chargingStartX;
        y = y - chargingStartY;
        calculateChargingAngle(x, y);
        calculateChargingPower(x, y);
    }

    private void calculateChargingAngle(float x, float y) {
        chargingAngle = (float) Math.atan2(x, y);
    }

    private void calculateChargingPower(float x, float y) {
        chargingPower = (float) (Math.sqrt(x * x + y * y) * CHARGING_MULTIPLIER);
        if (chargingPower > MAX_CHARGING_POWER) {
            chargingPower = MAX_CHARGING_POWER;
        }
    }

    public PointF getSpaceManSpeed() {
        float y = -1.0f * (float) Math.cos(chargingAngle) * chargingPower;
        float x = -1.0f * (float) Math.sin(chargingAngle) * chargingPower;
        return new PointF(x, y);
    }

    public float getAngle() {
        return chargingAngle;
    }

    public void reset() {
        chargingPower = 0;
        chargingAngle = 0;
    }
}