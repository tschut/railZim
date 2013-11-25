package com.spacemangames.railzim;

import android.content.Context;

public class GameThreadHolder {
    private static SpaceGameThread thread;

    // Create the thread
    public static SpaceGameThread createThread(Context context) {
        if (thread == null) {
            thread = new SpaceGameThread(context);
        }
        return thread;
    }

    public static SpaceGameThread getThread() {
        return thread;
    }
}
