package com.spacemangames.gravisphere;

public final class UnfreezeGameThreadRunnable implements Runnable {
    @Override
    public void run() {
        GameThreadHolder.getThread().unfreeze();
    }
}