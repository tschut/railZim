package com.spacemangames.gravisphere.ui;

import org.apache.commons.collections4.Predicate;

import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.gravisphere.SpaceGameThread;
import com.spacemangames.library.SpaceData;
import com.spacemangames.util.ThreadUtils;

@EBean
class PointsUpdateThread extends Thread {
    private final class GameNotStartedPredicate implements Predicate<Void> {
        @Override
        public boolean evaluate(Void object) {
            return SpaceGameState.INSTANCE.getState().isStarted();
        }
    }

    private final class FrameTimeElapsedPredicate implements Predicate<Void> {
        @Override
        public boolean evaluate(Void object) {
            return elapsedTime() >= SpaceGameThread.MIN_FRAME_TIME;
        }
    }

    private long       lastTime;

    @ViewById
    protected TextView pointsView;

    public PointsUpdateThread() {
        lastTime = System.nanoTime();
    }

    @Override
    public void run() {
        while (true) {
            ThreadUtils.sleepUntil(new GameNotStartedPredicate(), 100, null);
            ThreadUtils.sleepUntil(new FrameTimeElapsedPredicate(), (long) ((SpaceGameThread.MIN_FRAME_TIME - elapsedTime()) * 1000), null);

            lastTime = System.nanoTime();

            updatePointsView();
        }
    }

    private float elapsedTime() {
        return (System.nanoTime() - lastTime) / 1000000000f;
    }

    @UiThread
    protected void updatePointsView() {
        pointsView.setText(Integer.toString(SpaceData.getInstance().points.getCurrentPoints()));
    }
}