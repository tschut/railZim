package com.spacemangames.framework;

import com.badlogic.gdx.math.Vector2;
import com.spacemangames.pal.PALManager;

/** Singleton class describing the game state */
public class SpaceGameState {
    public class ChargingState {
        public static final int   MAX_CHARGING_POWER  = 150;
        public static final float CHARGING_MULTIPLIER = 1.3f;

        /** Start of charging gesture */
        private float             chargingStartX;
        private float             chargingStartY;

        /** Current pos of charging gesture */
        private float             chargingPower      = 0;
        private float             chargingAngle      = 0;

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

    private static final String TAG = "SpaceGameState";

    // private constructor
    private SpaceGameState() {
        state = STATE_INVALID;
    }

    // Singleton holder
    private static class SingletonHolder {
        public static final SpaceGameState INSTANCE = new SpaceGameState();
    }

    // Singleton access
    public static SpaceGameState getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // Interesting stuff follows
    // state for charging stuff
    public ChargingState    chargingState    = new ChargingState();

    /** Used to figure out elapsed time between frames */
    private long            lastTime;

    // Possible states
    public static final int STATE_INVALID     = -1;
    public static final int STATE_LOADING     = 0;
    public static final int STATE_LOADED      = 1;
    public static final int STATE_NOT_STARTED = 2;
    public static final int STATE_CHARGING    = 3;
    public static final int STATE_FLYING      = 4;
    public static final int STATE_PAUSED      = 5;

    public static final int NOT_YET_ENDED     = -1;
    public static final int WON_BRONZE        = 0;
    public static final int WON_SILVER        = 1;
    public static final int WON_GOLD          = 2;
    public static final int LOST_DIE          = 3;
    public static final int LOST_LOST         = 4;

    private int             endState         = NOT_YET_ENDED;

    private int             state;
    private int             lastState;

    private boolean         predicting       = false;

    public void setPredicting(boolean aPredicting) {
        predicting = aPredicting;
    }

    public boolean isPredicting() {
        return predicting;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized int getLastState() {
        return lastState;
    }

    public synchronized void setState(int aState) {
        if (aState == STATE_PAUSED) { // use setPaused (true) for this
            PALManager.getLog().e(TAG, "Trying to setState(STATE_PAUSED). Use setPaused(true) instead");
        }

        PALManager.getLog().i(TAG, "Changing state from " + getStateString(state) + " to " + getStateString(aState));

        updateTimeTick();

        if (state == STATE_PAUSED) {
            lastState = aState;
        } else {
            state = aState;
        }
    }

    public synchronized void setPaused(boolean aPause) {
        assert state >= STATE_LOADED;

        if (aPause) {
            PALManager.getLog().i(TAG, "Pausing. Current state: " + getStateString(state));
            if (state != STATE_PAUSED)
                lastState = state;
            state = STATE_PAUSED;
        } else {
            updateTimeTick();
            if (state != STATE_PAUSED) {
                PALManager.getLog().i(TAG, "Resuming while not paused, ignoring.");
            } else {
                PALManager.getLog().i(TAG, "Resuming. Setting state to: " + getStateString(lastState));
                state = lastState;
            }
        }
    }

    public synchronized boolean paused() {
        return (state == STATE_PAUSED);
    }

    public void togglePause() {
        if (state == STATE_PAUSED)
            setPaused(false);
        else
            setPaused(true);
    }

    public synchronized String getStateString(int aState) {
        String lRes = "";

        switch (aState) {
        case STATE_LOADING:
            lRes = "STATE_LOADING";
            break;
        case STATE_LOADED:
            lRes = "STATE_LOADED";
            break;
        case STATE_NOT_STARTED:
            lRes = "STATE_NOT_STARTED";
            break;
        case STATE_CHARGING:
            lRes = "STATE_CHARGING";
            break;
        case STATE_FLYING:
            lRes = "STATE_FLYING";
            break;
        case STATE_PAUSED:
            lRes = "STATE_PAUSED";
            break;
        default:
            lRes = "Unknown state!";
            break;
        }
        return lRes;
    }

    public synchronized float getElapsedTime() {
        long lNow = System.nanoTime();
        return (lNow - lastTime) / 1000000000f;
    }

    public synchronized void updateTimeTick() {
        lastTime = System.nanoTime();
    }

    public void setEndState(int aState) {
        endState = aState;
    }

    public int endState() {
        return endState;
    }
}
