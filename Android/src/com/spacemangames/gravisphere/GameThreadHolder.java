package com.spacemangames.gravisphere;

public class GameThreadHolder {
    private static SpaceGameThread mThread;

    // Create the thread
    public static SpaceGameThread createThread() {
        if (mThread == null) {
            mThread = new SpaceGameThread();
        }
        return mThread;
    }

    public static SpaceGameThread getThread() {
        return mThread;
    }
}
