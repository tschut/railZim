package com.games.spaceman.PAL;

import com.spacemangames.pal.ILog;

public class AndroidLog implements ILog {
    public int d (String tag, String msg) {
        return android.util.Log.d (tag, msg);
    }
    
    public int i (String tag, String msg) {
        return android.util.Log.i (tag, msg);
    }
    
    public int v (String tag, String msg) {
        return android.util.Log.v (tag, msg);
    }
    
    public int e (String tag, String msg) {
        return android.util.Log.e (tag, msg);
    }
    
    public int w (String tag, String msg) {
        return android.util.Log.w (tag, msg);
    }
}
