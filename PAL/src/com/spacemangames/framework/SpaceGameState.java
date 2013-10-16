package com.spacemangames.framework;

import com.spacemangames.pal.PALManager;

public class SpaceGameState {
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
