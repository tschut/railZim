package com.spacemangames.framework;

public enum GameState {
    INVALID,
    LOADING,
    LOADED,
    NOT_STARTED,
    CHARGING,
    FLYING,
    PAUSED;

    public boolean isStarted() {
        return this == CHARGING || this == FLYING || this == PAUSED;
    }

    public boolean isDoneLoading() {
        return this != INVALID && this != LOADING;
    }
}
