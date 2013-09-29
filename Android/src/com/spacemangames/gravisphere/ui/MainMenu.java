package com.spacemangames.gravisphere.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.gravisphere.LevelDbAdapter;
import com.spacemangames.gravisphere.LevelSelect;
import com.spacemangames.gravisphere.R;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.pal.PALManager;

@EActivity(R.layout.mainmenu_layout)
public class MainMenu extends Activity {
    private static final String TAG = Activity.class.getSimpleName();

    @ViewById
    protected Button            playButton;

    @ViewById
    protected Button            helpButton;

    @ViewById
    protected Button            listButton;

    @Click(R.id.playButton)
    protected void onClickPlay() {
        PALManager.getLog().v(TAG, "OnClick playButton");
        GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                GameThreadHolder.getThread().freeze();
            }
        });
        Intent i = new Intent(SpaceApp.mAppContext, SpaceApp.class);
        i.putExtra("level", SpaceApp.LAST_UNLOCKED_LEVEL);
        startActivity(i);
    }

    @Click(R.id.listButton)
    protected void onClickList() {
        PALManager.getLog().v(TAG, "OnClick listButton");
        GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                GameThreadHolder.getThread().freeze();
            }
        });
        Intent intent = new Intent(SpaceApp.mAppContext, LevelSelect.class);
        startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
    }

    @Click(R.id.helpButton)
    protected void onClickHelp() {
        PALManager.getLog().v(TAG, "OnClick helpButton");
        GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                GameThreadHolder.getThread().freeze();
            }
        });
        Intent intent = new Intent(SpaceApp.mAppContext, HelpActivity.class);
        startActivityForResult(intent, SpaceApp.ACTIVITY_HELP);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // we are being restored: restart the app
            Intent i = new Intent(this, com.spacemangames.gravisphere.ui.LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        PALManager.getLog().v(TAG, "onResume");

        GoogleAnalyticsTracker.getInstance().trackPageView("/mainmenu");

        super.onResume();

        if (goToHelpImmediately()) {
            GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
                @Override
                public void run() {
                    GameThreadHolder.getThread().freeze();
                }
            });
            Intent intent = new Intent(SpaceApp.mAppContext, HelpActivity.class);
            startActivityForResult(intent, SpaceApp.ACTIVITY_HELP);
        } else {
            SpaceView spaceView = (SpaceView) findViewById(R.id.space);
            spaceView.ignoreInput(true);

            GameThreadHolder.getThread().setSurfaceHolder(spaceView.getHolder());
            GameThreadHolder.getThread().changeLevel(SpaceLevel.ID_LOADING_SCREEN, true);
            GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
                @Override
                public void run() {
                    GameThreadHolder.getThread().unfreeze();
                }
            });
            SpaceGameState.getInstance().setPaused(false);
        }
    }

    @Override
    protected void onPause() {
        PALManager.getLog().v(TAG, "onPause");
        super.onPause();
        GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
            @Override
            public void run() {
                GameThreadHolder.getThread().freeze();
            }
        });
    }

    @Override
    public void onBackPressed() {
        PALManager.getLog().d(TAG, "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @Override
    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        PALManager.getLog().v(TAG, "onActivityResult");
        switch (aRequestCode) {
        case SpaceApp.ACTIVITY_LEVELSELECT:
            if (aResultCode == Activity.RESULT_OK) {
                GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
                    @Override
                    public void run() {
                        GameThreadHolder.getThread().freeze();
                    }
                });

                int level = aData.getIntExtra(SpaceApp.LEVEL_ID_STRING, 0);
                Intent i = new Intent(SpaceApp.mAppContext, SpaceApp.class);
                i.putExtra("level", level);
                startActivity(i);
            }
            break;
        case SpaceApp.ACTIVITY_HELP:
            if (aResultCode == Activity.RESULT_OK) {
                int action = aData.getIntExtra("action", HelpActivity.HELP_ACTION_START_GAME);
                if (action == HelpActivity.HELP_ACTION_START_GAME) {
                    GameThreadHolder.getThread().postSyncRunnable(new Runnable() {
                        @Override
                        public void run() {
                            GameThreadHolder.getThread().freeze();
                        }
                    });
                    startActivity(new Intent(SpaceApp.mAppContext, SpaceApp.class));
                }
            }
            break;
        }
    }

    // if this returns true we should skip the menu and go to the help
    // immediately
    private boolean goToHelpImmediately() {
        // return true if the first level is never completed...
        if (LevelDbAdapter.getInstance().highScore(0) > 0)
            return false;

        // ... and the shared preferences indicate we've never showed the help
        // before
        SharedPreferences sp = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        boolean hasSeenHelp = sp.getBoolean(HelpActivity.HAS_SEEN_HELP_SHARED_PREF_KEY, false);

        return !hasSeenHelp;
    }
}
