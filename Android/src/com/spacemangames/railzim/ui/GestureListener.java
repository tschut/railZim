package com.spacemangames.railzim.ui;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.spacemangames.framework.GameState;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.library.SpaceData;
import com.spacemangames.math.PointF;
import com.spacemangames.railzim.GameThreadHolder;

public class GestureListener implements OnGestureListener {

    private static final float FLING_MULTIPLICATION_FACTOR = 0.2f;

    @Override
    public boolean onDown(MotionEvent event) {
        boolean result = false;
        boolean hitsSpaceMan = GameThreadHolder.getThread().hitsSpaceMan(event.getX(), event.getY());
        boolean hitsArrow = GameThreadHolder.getThread().hitsSpaceManArrow(event.getX(), event.getY());
        GameState state = SpaceGameState.INSTANCE.getState();

        if (state == GameState.NOT_STARTED && hitsSpaceMan) {
            float x = SpaceUtil.resolutionScale(event.getX());
            float y = SpaceUtil.resolutionScale(event.getY());
            SpaceGameState.INSTANCE.setState(GameState.CHARGING);
            SpaceGameState.INSTANCE.chargingState.setChargingStart(x, y);
            SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            result = true;
        } else if (state == GameState.NOT_STARTED && hitsArrow) {
            GameThreadHolder.getThread().viewport.focusOn(SpaceData.getInstance().currentLevel.startCenter());
            result = true;
        } else if (hitsArrow) {
            GameThreadHolder.getThread().viewport.focusOn(SpaceData.getInstance().currentLevel.getSpaceManObject().getPosition());
            result = true;
        }

        return result;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        PointF flingSpeed = new PointF(-velocityX * FLING_MULTIPLICATION_FACTOR, -velocityY * FLING_MULTIPLICATION_FACTOR);
        GameThreadHolder.getThread().viewport.setFlinging(flingSpeed);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        // not used
    }

    @Override
    public boolean onScroll(MotionEvent event, MotionEvent event2, float distanceX, float distanceY) {
        boolean result = false;
        GameState state = SpaceGameState.INSTANCE.getState();

        if (state == GameState.CHARGING) {
            float x = SpaceUtil.resolutionScale(event2.getX());
            float y = SpaceUtil.resolutionScale(event2.getY());
            SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            result = true;
        } else {
            GameThreadHolder.getThread().viewport.moveViewport(distanceX, distanceY);
        }

        return result;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        // not used
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        // not used
        return false;
    }

    public boolean onUp(MotionEvent event) {
        boolean result = false;
        GameState state = SpaceGameState.INSTANCE.getState();

        if (state == GameState.CHARGING) {
            float x = SpaceUtil.resolutionScale(event.getX());
            float y = SpaceUtil.resolutionScale(event.getY());
            SpaceGameState.INSTANCE.chargingState.setChargingCurrent(x, y);
            GameThreadHolder.getThread().postRunnable(GameThreadHolder.getThread().new FireSpacemanRunnable());
            result = true;
        }

        return result;
    }
}
