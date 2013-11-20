package com.spacemangames.railzim;

public final class UnfreezeGameThreadRunnable implements Runnable {
    @Override
    public void run() {
        GameThreadHolder.getThread().unfreeze();
    }
}