package com.spacemangames.railzim;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.googlecode.androidannotations.annotations.EBean;
import com.spacemangames.library.SpaceData;
import com.spacemangames.railzim.ui.LevelListItemView;
import com.spacemangames.railzim.ui.LevelListItemView_;

@EBean
public class LevelListAdapter extends BaseAdapter {
    private Context context;

    public LevelListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return SpaceData.getInstance().levels.size();
    }

    @Override
    public Object getItem(int position) {
        return SpaceData.getInstance().levels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String levelTitle = "title";
        String levelNumber = "number";
        String levelHighScore = "1234";

        LevelListItemView levelListItemView;
        if (convertView == null) {
            levelListItemView = LevelListItemView_.build(context);
        } else {
            levelListItemView = (LevelListItemView) convertView;
        }

        levelListItemView.setData(levelTitle, levelNumber, levelHighScore);

        return levelListItemView;
    }
}
