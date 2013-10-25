package com.spacemangames.gravisphere;

import com.spacemangames.library.SpaceData;

public class GameThreadHolder {
    private static SpaceGameThread mThread;

    // Create the thread
    public static SpaceGameThread createThread() {
        if (mThread == null) {
            mThread = new SpaceGameThread(SpaceData.getInstance());
        }
        return mThread;
    }

    public static SpaceGameThread getThread() {
        return mThread;
    }
}
