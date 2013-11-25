package com.spacemangames.railzim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.EActivity;
import com.spacemangames.framework.ILoadingDoneListener;
import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.library.SpaceData;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.EmptyLog;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.DebugSettings;
import com.spacemangames.railzim.GameThreadHolder;
import com.spacemangames.railzim.R;
import com.spacemangames.railzim.SpaceGameThread;
import com.spacemangames.railzim.pal.AndroidBitmapFactory;
import com.spacemangames.railzim.pal.AndroidLog;
import com.spacemangames.railzim.pal.AndroidResourceHandler;
import com.spacemangames.util.ThreadUtils;

@EActivity(R.layout.loading_layout)
public class LoadingActivity extends Activity implements ILoadingDoneListener {
    private long startLoadingTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startLoadingTime = System.currentTimeMillis();

        bootstrap();

        // start the game thread
        final SpaceGameThread thread = GameThreadHolder.createThread(getApplicationContext());
        thread.setRunning(true);
        thread.freeze();
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }

        thread.postRunnable(new Runnable() {
            @Override
            public void run() {
                SpaceData.getInstance().preloadAllLevels();
                // load the fist level
                thread.changeLevel(0, true);
                SpaceData.getInstance().setLoadingDone();
            }
        });
    }

    private void bootstrap() {
        initializePAL();
        initDisplay();

        SpaceApp.mAppContext = this;
        GoogleAnalyticsTracker.getInstance().startNewSession("UA-34397887-2", 30, this);
        SpaceData.getInstance().addLoadingDoneListener(this);
    }

    private void initDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        SpaceUtil.init(dm.xdpi, dm.ydpi);
        Rect resolution = new Rect(0, 0, dm.widthPixels, dm.heightPixels);
        SpaceUtil.setResolution(resolution);
    }

    private void initializePAL() {
        PALManager.setResourceHandler(new AndroidResourceHandler());
        PALManager.setBitmapFactory(new AndroidBitmapFactory());
        if (DebugSettings.DEBUG_LOGGING) {
            PALManager.setLog(new AndroidLog());
        } else {
            PALManager.setLog(new EmptyLog());
        }
    }

    @Override
    public void loadingDone() {
        SpaceData.getInstance().remLoadingDoneListener(this);

        while (System.currentTimeMillis() - startLoadingTime < 3000) {
            ThreadUtils.silentSleep(100);
        }

        startActivity(new Intent(SpaceApp.mAppContext, MainMenu_.class));
    }
}
