package com.spacemangames.railzim;

import android.content.Context;

public class GameThreadHolder {
    private static SpaceGameThread mThread;

    // Create the thread
    public static SpaceGameThread createThread(Context context) {
        if (mThread == null) {
            mThread = SpaceGameThread_.getInstance_(context);
        }
        return mThread;
    }

    public static SpaceGameThread getThread() {
        return mThread;
    }
}
