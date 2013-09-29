package com.spacemangames.gravisphere;

public final class FreezeGameThreadRunnable implements Runnable {
    @Override
    public void run() {
        GameThreadHolder.getThread().freeze();
    }
}