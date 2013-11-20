package com.spacemangames.railzim;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.googlecode.androidannotations.annotations.EBean;
import com.spacemangames.library.SpaceData;
import com.spacemangames.railzim.contentprovider.HighScoresContentProvider;
import com.spacemangames.railzim.contentprovider.LevelDbAdapter;
import com.spacemangames.railzim.ui.LevelListItemView;
import com.spacemangames.railzim.ui.LevelListItemView_;

@EBean
public class LevelListAdapter extends BaseAdapter {
    private Context context;
    private Cursor  cursor;

    public LevelListAdapter(Context context) {
        this.context = context;

        cursor = context.getContentResolver().query(HighScoresContentProvider.CONTENT_URI, null, null, null, null);
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
