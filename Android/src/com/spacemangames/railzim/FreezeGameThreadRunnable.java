package com.spacemangames.railzim;

public final class FreezeGameThreadRunnable implements Runnable {
    @Override
    public void run() {
        GameThreadHolder.getThread().freeze();
    }
}