package com.spacemangames.gravisphere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

@EFragment(R.layout.end_level_layout)
public class EndLevelDialogFragment extends DialogFragment {
    @ViewById
    protected Button    retryButton;

    @ViewById
    protected Button    levelListButton;

    @ViewById
    protected Button    nextLevelButton;

    @ViewById
    protected TextView  subtitleTextView;

    @ViewById
    protected ImageView starImageView;

    @ViewById
    protected TextView  pointsTextView;

    @ViewById
    protected TextView  highscoreTextView;

    @Click(R.id.retryButton)
    protected void retryLevel() {
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
        GameThreadHolder.getThread().reloadCurrentLevel();
        Intent intent = new Intent(activity, LevelSelect.class);
        activity.startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
        dismiss();
    }

    @Click(R.id.nextLevelButton)
    protected void gotoNextLevel() {
        GameThreadHolder.getThread().loadNextLevel();
        GameThreadHolder.getThread().redrawOnce();
        dismiss();
    }

    private Activity activity;
    private int      textResource;
    private int      titleResource;
    private int      imageResource;
    private int      best;
    private int      points;
    private boolean  nextLevelUnlocked;

    public void setStartingActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @AfterViews
    protected void init() {
        getDialog().setTitle(titleResource);
        nextLevelButton.setEnabled(nextLevelUnlocked);
        subtitleTextView.setText(textResource);
        starImageView.setImageResource(imageResource);
        pointsTextView.setText(Integer.toString(points));
        highscoreTextView.setText(Integer.toString(best));
    }

    public void setProperties(int points, int best, int imageResource, int titleResource, int textResource, boolean nextLevelUnlocked) {
        this.points = points;
        this.best = best;
        this.imageResource = imageResource;
        this.titleResource = titleResource;
        this.textResource = textResource;
        this.nextLevelUnlocked = nextLevelUnlocked;
    }
}
