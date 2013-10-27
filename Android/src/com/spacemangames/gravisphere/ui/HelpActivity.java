package com.spacemangames.gravisphere.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.spacemangames.gravisphere.FreezeGameThreadRunnable;
import com.spacemangames.gravisphere.GamePrefs_;
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.gravisphere.R;
import com.spacemangames.gravisphere.UnfreezeGameThreadRunnable;
import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.pal.PALManager;

@EActivity(R.layout.help_layout)
public class HelpActivity extends Activity {
    private static final String TAG                    = "HelpActivity";

    public static final int     HELP_ACTION_START_GAME = 0;

    @ViewById
    protected Button            nextButton;

    @ViewById
    protected Button            prevButton;

    @Pref
    protected GamePrefs_        gamePrefs;

    @Click(R.id.prevButton)
    protected void onClickPrev() {
        PALManager.getLog().v(TAG, "OnClick PrevButton");
        if (SpaceData.getInstance().mCurrentLevel.mId == SpaceLevel.ID_HELP1) {
            SpaceView spaceview = (SpaceView) findViewById(R.id.space);
            spaceview.ignoreFocusChange = true;
            finish();
        } else {
            GameThreadHolder.getThread().loadPrevLevel(true);
        }
    }

    @Click(R.id.nextButton)
    protected void onClickNext() {
        PALManager.getLog().v(TAG, "OnClick NextButton");
        if (SpaceData.getInstance().mCurrentLevel.mId == SpaceLevel.ID_HELP4) {
            GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
            Intent i = new Intent();
            i.putExtra("action", HELP_ACTION_START_GAME);
            setResult(Activity.RESULT_OK, i);
            finish();
        } else {
            GameThreadHolder.getThread().loadNextLevel(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            gamePrefs.hasSeenHelp().put(true);
        } else {
            // we are being restored: restart the app
            Intent i = new Intent(this, LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsTracker.getInstance().trackPageView("/help");

        PALManager.getLog().v(TAG, "onResume");
        super.onResume();

        SpaceView spaceView = (SpaceView) findViewById(R.id.space);
        spaceView.ignoreInput(true);

        GameThreadHolder.getThread().setSurfaceHolder(spaceView.getHolder());
        GameThreadHolder.getThread().changeLevel(SpaceLevel.ID_HELP1, true);
        GameThreadHolder.getThread().postSyncRunnable(new UnfreezeGameThreadRunnable());
    }

    @Override
    protected void onPause() {
        PALManager.getLog().v(TAG, "onPause");
        super.onPause();
        GameThreadHolder.getThread().postSyncRunnable(new FreezeGameThreadRunnable());
    }
}
