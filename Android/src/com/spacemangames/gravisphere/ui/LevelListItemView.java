package com.spacemangames.gravisphere.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.framework.EndGameState;
import com.spacemangames.gravisphere.LevelDbAdapter;
import com.spacemangames.gravisphere.R;
import com.spacemangames.library.SpaceData;

@EViewGroup(R.layout.levelselect_item)
public class LevelListItemView extends LinearLayout {
    @ViewById
    protected TextView  levelTitle;
    @ViewById
    protected TextView  levelNumber;
    @ViewById
    protected TextView  levelPoints;
    @ViewById
    protected TextView  levelPointsLabel;
    @ViewById
    protected ImageView starImage;

    public LevelListItemView(Context context) {
        super(context);
    }

    public void setData(String levelTitle, String levelNumber, String levelPoints) {
        this.levelTitle.setText(levelTitle);
        this.levelNumber.setText(levelNumber);
        this.levelPoints.setText(levelPoints);
        this.levelPoints.setVisibility(View.VISIBLE);
        this.levelPointsLabel.setVisibility(View.VISIBLE);

        if (!LevelDbAdapter.getInstance().levelIsUnlocked(Integer.parseInt(levelNumber))) {
            starImage.setImageResource(R.drawable.star_disabled);
            this.levelPoints.setVisibility(View.INVISIBLE);
            this.levelPointsLabel.setVisibility(View.INVISIBLE);
        } else if (Integer.parseInt(levelPoints) == 0) {
            starImage.setImageResource(R.drawable.star_enabled);
        } else {
            EndGameState endGameState = SpaceData.getInstance()
                    .levelStarColor(Integer.parseInt(levelNumber), Integer.parseInt(levelPoints));
            EndGameStatePresenter endGameStatePresenter = EndGameStatePresenter.valueOfEndGameState(endGameState);
            starImage.setImageResource(endGameStatePresenter.getStarImageResourceId());
        }
    }
}
