package com.spacemangames.pal;

public class EmptyLog implements ILog {
    @Override
    public int v(String tag, String msg) {
        return 0;
    }

    @Override
    public int i(String tag, String msg) {
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        return 0;
    }
}
