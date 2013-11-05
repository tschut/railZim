package com.spacemangames.gravisphere.ui;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.spacemangames.gravisphere.R;

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

    public void setData(String levelTitle, String levelNumber, String levelPoints, String levelPointsLabel, int starImageResource) {
        this.levelTitle.setText(levelTitle);
        this.levelNumber.setText(levelNumber);
        this.levelPoints.setText(levelPoints);
        this.levelPointsLabel.setText(levelPointsLabel);
        this.starImage.setImageResource(starImageResource);
    }
}
