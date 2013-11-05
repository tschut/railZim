package com.spacemangames.gravisphere.ui;

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
import com.spacemangames.gravisphere.GameThreadHolder;
import com.spacemangames.gravisphere.R;

@EFragment(R.layout.end_level_layout)
public class EndLevelDialogFragment extends DialogFragment {
    public static class EndLevelDialogFragmentData {
        private Activity activity;
        private int      message;
        private int      subtitle;
        private int      star;
        private int      best;
        private int      points;
        private boolean  nextLevelUnlocked;

        public EndLevelDialogFragmentData activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public EndLevelDialogFragmentData message(int message) {
            this.message = message;
            return this;
        }

        public EndLevelDialogFragmentData subtitle(int subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public EndLevelDialogFragmentData star(int star) {
            this.star = star;
            return this;
        }

        public EndLevelDialogFragmentData best(int best) {
            this.best = best;
            return this;
        }

        public EndLevelDialogFragmentData points(int points) {
            this.points = points;
            return this;
        }

        public EndLevelDialogFragmentData nextLevelUnlocked(boolean isUnlocked) {
            this.nextLevelUnlocked = isUnlocked;
            return this;
        }
    }

    @ViewById
    protected Button                   retryButton;

    @ViewById
    protected Button                   levelListButton;

    @ViewById
    protected Button                   nextLevelButton;

    @ViewById
    protected TextView                 subtitleTextView;

    @ViewById
    protected ImageView                starImageView;

    @ViewById
    protected TextView                 pointsTextView;

    @ViewById
    protected TextView                 highscoreTextView;

    private EndLevelDialogFragmentData data;

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
        Intent intent = LevelSelect_.intent(data.activity).get();
        data.activity.startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
        dismiss();
    }

    @Click(R.id.nextLevelButton)
    protected void gotoNextLevel() {
        GameThreadHolder.getThread().loadNextLevel();
        GameThreadHolder.getThread().redrawOnce();
        dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @AfterViews
    protected void init() {
        getDialog().setTitle(data.subtitle);
        nextLevelButton.setEnabled(data.nextLevelUnlocked);
        subtitleTextView.setText(data.message);
        starImageView.setImageResource(data.star);
        pointsTextView.setText(Integer.toString(data.points));
        highscoreTextView.setText(Integer.toString(data.best));
    }

    public void setProperties(EndLevelDialogFragmentData data) {
        this.data = data;
    }
}
