package com.spacemangames.railzim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OnActivityResult;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.FreezeGameThreadRunnable;
import com.spacemangames.railzim.GamePrefs_;
import com.spacemangames.railzim.GameThreadHolder;
import com.spacemangames.railzim.R;
import com.spacemangames.railzim.UnfreezeGameThreadRunnable;

@EActivity(R.layout.mainmenu_layout)
public class MainMenu extends Activity {
    private static final String TAG = Activity.class.getSimpleName();

    @ViewById
    protected Button            playButton;

    @ViewById
    protected Button            helpButton;

    @ViewById
    protected Button            listButton;

    @ViewById
    protected SpaceView         spaceView;

    @Pref
    protected GamePrefs_        gamePrefs;

    @Click(R.id.playButton)
    protected void onClickPlay() {
        PALManager.getLog().v(TAG, "OnClick playButton");
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
        Intent i = new Intent(SpaceApp.mAppContext, SpaceApp_.class);
        i.putExtra("level", SpaceApp.LAST_UNLOCKED_LEVEL);
        startActivity(i);
    }

    @Click(R.id.listButton)
    protected void onClickList() {
        PALManager.getLog().v(TAG, "OnClick listButton");
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
        Intent intent = LevelSelect_.intent(SpaceApp.mAppContext).get();
        startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
    }

    @Click(R.id.helpButton)
    protected void onClickHelp() {
        PALManager.getLog().v(TAG, "OnClick helpButton");
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
        Intent intent = new Intent(SpaceApp.mAppContext, HelpActivity_.class);
        startActivityForResult(intent, SpaceApp.ACTIVITY_HELP);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // we are being restored: restart the app
            Intent i = new Intent(this, com.spacemangames.railzim.ui.LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsTracker.getInstance().trackPageView("/mainmenu");

        super.onResume();

        if (goToHelpImmediately()) {
            GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
            Intent intent = HelpActivity_.intent(this).get();
            startActivityForResult(intent, SpaceApp.ACTIVITY_HELP);
        } else {
            spaceView.ignoreInput(true);

            GameThreadHolder.getThread().setSurfaceHolder(spaceView.getHolder());
            GameThreadHolder.getThread().changeLevel(SpaceLevel.ID_LOADING_SCREEN, true);
            GameThreadHolder.getThread().postSyncRunnable(new UnfreezeGameThreadRunnable());
            SpaceGameState.INSTANCE.setPaused(false);
        }
    }

    @Override
    protected void onPause() {
        PALManager.getLog().v(TAG, "onPause");
        super.onPause();
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
    }

    @Override
    public void onBackPressed() {
        PALManager.getLog().d(TAG, "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @OnActivityResult(SpaceApp.ACTIVITY_LEVELSELECT)
    protected void onLevelSelectResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());

            int level = data.getIntExtra(SpaceApp.LEVEL_ID_STRING, 0);
            Intent i = new Intent(SpaceApp.mAppContext, SpaceApp_.class);
            i.putExtra("level", level);
            startActivity(i);
        }
    }

    @OnActivityResult(SpaceApp.ACTIVITY_HELP)
    protected void onReturnFromHelp(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            int action = data.getIntExtra("action", HelpActivity.HELP_ACTION_START_GAME);
            if (action == HelpActivity.HELP_ACTION_START_GAME) {
                GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
                startActivity(new Intent(SpaceApp.mAppContext, SpaceApp_.class));
            }
        }
    }

    private boolean goToHelpImmediately() {
        return !gamePrefs.hasSeenHelp().getOr(false);
    }
}
