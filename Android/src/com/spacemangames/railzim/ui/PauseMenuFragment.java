package com.spacemangames.railzim.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.widget.Button;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.EndGameState;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.railzim.GameThreadHolder;
import com.spacemangames.railzim.R;

@EFragment(R.layout.pause_layout)
public class PauseMenuFragment extends DialogFragment {
    @ViewById
    protected Button levelListButton;

    @ViewById
    protected Button restartButton;

    @ViewById
    protected Button continueButton;

    private Activity activity;

    @Click(R.id.restartButton)
    protected void onRestart() {
        GameThreadHolder.getThread().reloadCurrentLevel();
        GameThreadHolder.getThread().redrawOnce();
        dismiss();
    }

    @Click(R.id.levelListButton)
    protected void gotoLevelList() {
        // Reload the current level. If we don't do that, the flow makes it
        // possible to get back to
        // the level in the state it's in now because you can press the
        // 'back'button in the level selector
        EndGameState state = SpaceGameState.INSTANCE.endState();
        if (state != EndGameState.NOT_ENDED) {
            GameThreadHolder.getThread().reloadCurrentLevel();
        }
        Intent intent = LevelSelect_.intent(activity).get();
        activity.startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
        dismiss();
    }

    @Click(R.id.continueButton)
    protected void onContinue() {
        SpaceGameState.INSTANCE.setPaused(false);
        dismiss();
    }

    public void setStartingActivity(Activity activity) {
        this.activity = activity;
    }

    @AfterViews
    protected void init() {
        getDialog().setTitle(R.string.pause_title);
    }
}
