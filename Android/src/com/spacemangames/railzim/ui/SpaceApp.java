package com.spacemangames.railzim.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.GameState;
import com.spacemangames.framework.ILevelChangedListener;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.library.SpaceData;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.DebugSettings;
import com.spacemangames.railzim.FreezeGameThreadRunnable;
import com.spacemangames.railzim.GameThreadHolder;
import com.spacemangames.railzim.R;
import com.spacemangames.railzim.UnfreezeGameThreadRunnable;
import com.spacemangames.railzim.ui.EndLevelDialogFragment.EndLevelDialogFragmentData;

@EActivity(R.layout.game_layout)
public class SpaceApp extends FragmentActivity implements ILevelChangedListener {
    public static final int        ACTIVITY_LEVELSELECT = 0;
    public static final int        ACTIVITY_HELP        = 1;

    public static final int        DIALOG_END_LEVEL     = 0;

    public static final int        LAST_UNLOCKED_LEVEL  = -2;

    public static final String     LEVEL_ID_STRING      = "com.games.spaceman.level_id";

    public static final int        MENU_LEVELSELECT_ID  = Menu.FIRST;
    public static final int        MENU_RESTARTLEVEL_ID = Menu.FIRST + 1;

    private static final String    TAG                  = "SpaceApp";

    public static Context          mAppContext;

    private static int             mRestoreLevel        = -1;

    private GoogleAnalyticsTracker tracker;

    @Bean
    protected PointsUpdateThread   pointsUpdateThread;

    @ViewById
    GameView                       gameView;

    // parse events that occurred. This is called after the physics have been
    // updated
    public Handler                 mMsgHandler          = new Handler() {
                                                            @Override
                                                            public void handleMessage(Message msg) {
                                                                showEndLevelDialog();
                                                            }
                                                        };

    private void showEndLevelDialog() {
        tracker.trackPageView("/endLevelDialog");

        int points = SpaceData.getInstance().points.getCurrentPoints();
        EndGameStatePresenter endState = EndGameStatePresenter.valueOfEndGameState(SpaceGameState.INSTANCE.endState());
        int imageResource = endState.getStarImageResourceId();
        int titleResource = endState.getTitleResourceId();
        int textResource = endState.getMsgResourceId();
        boolean nextLevelUnlocked = false;

        FragmentManager fm = getSupportFragmentManager();
        EndLevelDialogFragment endLevelDialog = new EndLevelDialogFragment_();
        EndLevelDialogFragmentData data = new EndLevelDialogFragmentData();
        data.activity(this).points(points).best(9999).star(imageResource).subtitle(titleResource).message(textResource)
                .nextLevelUnlocked(nextLevelUnlocked);
        endLevelDialog.setProperties(data);
        endLevelDialog.show(fm, "end_level_dialog");
    }

    @AfterViews
    protected void startPointsUpdateThread() {
        GameThreadHolder.getThread().setSurfaceHolder(gameView.getHolder());

        pointsUpdateThread.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DebugSettings.DEBUG_LOGGING) {
            Log.i(TAG, "onCreate");
        }

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            // nothing to do?
            PALManager.getLog().i(TAG, "Normal startup");

            tracker = GoogleAnalyticsTracker.getInstance();

            // register for level changed events
            SpaceData.getInstance().addLevelChangedListener(this);
        } else {
            // we are being restored: restart the app
            Intent i = new Intent(this, com.spacemangames.railzim.ui.LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (SpaceGameState.INSTANCE.getState() == GameState.FLYING)
            SpaceGameState.INSTANCE.setPaused(true);

        showPauseMenu();
        return true;
    }

    private void showPauseMenu() {
        tracker.trackPageView("/pauseDialog");

        FragmentManager fm = getSupportFragmentManager();
        PauseMenuFragment pauseMenu = new PauseMenuFragment_();
        pauseMenu.setStartingActivity(this);
        pauseMenu.show(fm, "pause_menu");
    }

    @Override
    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        PALManager.getLog().v(TAG, "onActivityResult");
        switch (aRequestCode) {
        case ACTIVITY_LEVELSELECT:
            if (aResultCode == Activity.RESULT_OK) {
                mRestoreLevel = aData.getIntExtra(LEVEL_ID_STRING, 0);
            }
        }
    }

    @Override
    protected void onPause() {
        PALManager.getLog().i(TAG, "onPause");
        if (SpaceGameState.INSTANCE.getState() == GameState.FLYING) {
            SpaceGameState.INSTANCE.setPaused(true);
        }
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
        mRestoreLevel = SpaceData.getInstance().getCurrentLevelId();
        PALManager.getLog().v(TAG, "storing level id " + mRestoreLevel);
        super.onPause();
    }

    @Override
    protected void onResume() {
        PALManager.getLog().i(TAG, "onResume");
        super.onResume();

        Intent i = getIntent();
        if (i != null) {
            mRestoreLevel = i.getIntExtra("level", mRestoreLevel);
            i.removeExtra("level");
        }

        GameThreadHolder.getThread().changeLevel(0);
        GameThreadHolder.getThread().postSyncRunnable(new UnfreezeGameThreadRunnable());
        GameThreadHolder.getThread().redrawOnce();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        PALManager.getLog().i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void LevelChanged(int aNewLevelID) {
        tracker.trackPageView("/levels/" + aNewLevelID);
    }
}