package com.spacemangames.gravisphere.ui;

import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.gravisphere.SpaceGameThread;
import com.spacemangames.library.SpaceData;

@EBean
class PointsUpdateThread extends Thread {
    private long       lastTime;
    public boolean     running = true;

    @ViewById
    protected TextView pointsView;

    public PointsUpdateThread() {
        lastTime = System.nanoTime();
    }

    @Override
    public void run() {
        while (running) {
            if (SpaceGameState.getInstance().getState() < SpaceGameState.STATE_NOT_STARTED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                }
                continue;
            }

            float elapsed = (System.nanoTime() - lastTime) / 1000000000f;

            if (elapsed < SpaceGameThread.MIN_FRAME_TIME) {
                try {
                    sleep((long) ((SpaceGameThread.MIN_FRAME_TIME - elapsed) * 1000));
                    continue;
                } catch (InterruptedException e) {
                }
            }
            lastTime = System.nanoTime();

            updatePointsView();
        }
    }

    @UiThread
    protected void updatePointsView() {
        pointsView.setText(Integer.toString(SpaceData.getInstance().points.getCurrentPoints()));
    }
}