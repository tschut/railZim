package com.games.spaceman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.spacemangames.gravisphere.R;

public class EndLevelDialogFragment extends DialogFragment {
    private final class OnRetryClickListener implements View.OnClickListener {
        public void onClick(View v) {
            GameThreadHolder.getThread().reloadCurrentLevel();
            GameThreadHolder.getThread().redrawOnce();
            dismiss();
        }
    }

    private final class OnLevelListClickListener implements View.OnClickListener {
        public void onClick(View v) {
            // Reload the current level. If we don't do that, the flow makes it possible to get back to
            // the level in the state it's in now because you can press the 'back'button in the level selector
            GameThreadHolder.getThread().reloadCurrentLevel();
            Intent intent = new Intent(activity, LevelSelect.class);
            activity.startActivityForResult(intent, SpaceApp.ACTIVITY_LEVELSELECT);
            dismiss();
        }
    }

    private final class OnNextLevelClickListener implements View.OnClickListener {
        public void onClick(View v) {
            GameThreadHolder.getThread().loadNextLevel();
            GameThreadHolder.getThread().redrawOnce();
            dismiss();
        }
    }

    private Activity activity;
    private AdView adView;
    private int textResource;
    private int titleResource;
    private int imageResource;
    private int best;
    private int points;
    private boolean nextLevelUnlocked;

    public void setStartingActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.end_level_layout, container);

        LinearLayout adLayout = (LinearLayout) view.findViewById(R.id.adLayout);

        adView = new AdView(activity, AdSize.BANNER, AdSettings.ADSENSE_ID);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AdSize.BANNER.getWidth(), displayMetrics));
        int height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AdSize.BANNER.getHeight(), displayMetrics));
        adLayout.addView(adView, width, height);
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdSettings.TEST_DEVICE_ID);
        adView.loadAd(adRequest);

        getDialog().setTitle(titleResource);
        ((TextView) view.findViewById(R.id.end_level_subtitle)).setText(textResource);
        ((ImageView) view.findViewById(R.id.end_level_star)).setImageResource(imageResource);
        ((TextView) view.findViewById(R.id.end_level_points)).setText(Integer.toString(points));
        ((TextView) view.findViewById(R.id.end_level_points_best)).setText(Integer.toString(best));

        Button next = (Button) view.findViewById(R.id.end_level_button_next);
        Button list = (Button) view.findViewById(R.id.end_level_button_list);
        Button retry = (Button) view.findViewById(R.id.end_level_button_retry);

        next.setEnabled(nextLevelUnlocked);

        next.setOnClickListener(new OnNextLevelClickListener());
        list.setOnClickListener(new OnLevelListClickListener());
        retry.setOnClickListener(new OnRetryClickListener());

        return view;
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void setProperties(int points, int best, int imageResource, int titleResource, int textResource,
            boolean nextLevelUnlocked) {
        this.points = points;
        this.best = best;
        this.imageResource = imageResource;
        this.titleResource = titleResource;
        this.textResource = textResource;
        this.nextLevelUnlocked = nextLevelUnlocked;

    }
}
