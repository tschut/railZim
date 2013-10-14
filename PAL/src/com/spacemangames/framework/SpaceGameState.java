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

    private static final String TAG = "SpaceGameState";

    private SpaceGameState() {
        state = GameState.INVALID;
    }

    private static class SingletonHolder {
        public static final SpaceGameState INSTANCE = new SpaceGameState();
    }

    public static SpaceGameState getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ChargingState chargingState = new ChargingState();

    private long         lastTime;

    private EndGameState endState      = EndGameState.NOT_ENDED;

    private GameState    state;
    private GameState    lastState;

    private boolean      predicting    = false;

    public void setPredicting(boolean aPredicting) {
        predicting = aPredicting;
    }

    public boolean isPredicting() {
        return predicting;
    }

    public synchronized GameState getState() {
        return state;
    }

    public synchronized GameState getLastState() {
        return lastState;
    }

    public synchronized void setState(GameState state) {
        if (state == GameState.PAUSED) { // use setPaused (true) for this
            PALManager.getLog().e(TAG, "Trying to setState(STATE_PAUSED). Use setPaused(true) instead");
        }

        PALManager.getLog().i(TAG, "Changing state from " + state + " to " + state);

        updateTimeTick();

        if (state == GameState.PAUSED) {
            lastState = state;
        } else {
            this.state = state;
        }
    }

    public synchronized void setPaused(boolean pause) {
        if (pause) {
            PALManager.getLog().i(TAG, "Pausing. Current state: " + state);
            if (state != GameState.PAUSED) {
                lastState = state;
            }
            state = GameState.PAUSED;
        } else {
            updateTimeTick();
            if (state != GameState.PAUSED) {
                PALManager.getLog().i(TAG, "Resuming while not paused, ignoring.");
            } else {
                PALManager.getLog().i(TAG, "Resuming. Setting state to: " + lastState);
                state = lastState;
            }
        }
    }

    public synchronized boolean paused() {
        return (state == GameState.PAUSED);
    }

    public void togglePause() {
        if (state == GameState.PAUSED)
            setPaused(false);
        else
            setPaused(true);
    }

    public synchronized float getElapsedTime() {
        long lNow = System.nanoTime();
        return (lNow - lastTime) / 1000000000f;
    }

    public synchronized void updateTimeTick() {
        lastTime = System.nanoTime();
    }

    public void setEndState(EndGameState state) {
        endState = state;
    }

    public EndGameState endState() {
        return endState;
    }
}
