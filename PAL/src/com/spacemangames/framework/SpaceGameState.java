package com.spacemangames.framework;

import com.spacemangames.pal.PALManager;

public enum SpaceGameState {
    INSTANCE;

    private static final String TAG = SpaceGameState.class.getSimpleName();

    private SpaceGameState() {
        state = GameState.INVALID;
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

    public GameState getState() {
        return state;
    }

    public GameState getLastState() {
        return lastState;
    }

    public synchronized void setState(GameState state) {
        PALManager.getLog().i(TAG, "Changing state from " + state + " to " + state);

        updateTimeTick();
        this.state = state;
    }

    public synchronized void setPaused(boolean pause) {
        if (pause) {
            pause();
        } else {
            unpause();
        }
    }

    private void unpause() {
        updateTimeTick();
        if (state == GameState.PAUSED) {
            PALManager.getLog().i(TAG, "Resuming. Setting state to: " + lastState);
            state = lastState;
        }
    }

    private void pause() {
        if (state != GameState.PAUSED) {
            PALManager.getLog().i(TAG, "Pausing. Current state: " + state);
            lastState = state;
            state = GameState.PAUSED;
        }
    }

    public boolean paused() {
        return (state == GameState.PAUSED);
    }

    public synchronized float getElapsedTime() {
        return (System.nanoTime() - lastTime) / 1000000000f;
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
