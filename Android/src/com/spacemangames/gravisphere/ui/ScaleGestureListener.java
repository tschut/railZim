package com.spacemangames.gravisphere.ui;

import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import com.spacemangames.gravisphere.GameThreadHolder;

public class ScaleGestureListener implements OnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        GameThreadHolder.getThread().viewport.zoomViewport(-(detector.getScaleFactor() - 1));
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }
}
