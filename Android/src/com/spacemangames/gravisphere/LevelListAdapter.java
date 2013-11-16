package com.spacemangames.gravisphere;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.spacemangames.gravisphere.contentprovider.LevelDbAdapter;
import com.spacemangames.gravisphere.ui.LevelListItemView;
import com.spacemangames.gravisphere.ui.LevelListItemView_;
import com.spacemangames.library.SpaceData;

public class LevelListAdapter extends BaseAdapter {
    private final Cursor cursor;
    private Context      context;

    public LevelListAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
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
        cursor.moveToPosition((int) getItemId(position));
        String levelTitle = cursor.getString(cursor.getColumnIndex(LevelDbAdapter.KEY_TITLE));
        String levelNumber = cursor.getString(cursor.getColumnIndex(LevelDbAdapter.KEY_LEVELNUMBER));
        String levelHighScore = cursor.getString(cursor.getColumnIndex(LevelDbAdapter.KEY_HIGHSCORE));

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
