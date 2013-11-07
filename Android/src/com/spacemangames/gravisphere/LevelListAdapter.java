package com.spacemangames.gravisphere;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.spacemangames.framework.EndGameState;
import com.spacemangames.gravisphere.ui.EndGameStatePresenter;
import com.spacemangames.gravisphere.ui.LevelListItemView;
import com.spacemangames.gravisphere.ui.LevelListItemView_;
import com.spacemangames.library.SpaceData;
import com.spacemangames.pal.PALManager;

public class LevelListAdapter extends BaseAdapter {
    private static final String TAG            = LevelListAdapter.class.getSimpleName();

    private static final int    TYPE_LEVEL     = 0;
    private static final int    TYPE_AD        = 1;
    private static final int    TYPE_MAX_COUNT = TYPE_AD + 1;

    private final Cursor        cursor;

    private Context             context;

    public LevelListAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_LEVEL;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
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
        String levelHighScoreLabel = "Score: ";

        PALManager.getLog().i(TAG, "Level: " + levelNumber + " " + levelHighScore + " points");

        LevelListItemView levelListItemView;
        if (convertView == null) {
            levelListItemView = LevelListItemView_.build(context);
        } else {
            levelListItemView = (LevelListItemView) convertView;
        }

        int starImageResource;
        if (!LevelDbAdapter.getInstance().levelIsUnlocked(Integer.parseInt(levelNumber))) {
            starImageResource = R.drawable.star_disabled;
            levelHighScore = "";
            levelHighScoreLabel = "";
        } else if (!levelHighScore.isEmpty() && Integer.parseInt(levelHighScore) == 0) {
            starImageResource = R.drawable.star_enabled;
        } else {
            EndGameState endGameState = SpaceData.getInstance().levelStarColor(Integer.parseInt(levelNumber),
                    Integer.parseInt(levelHighScore));
            EndGameStatePresenter endGameStatePresenter = EndGameStatePresenter.valueOfEndGameState(endGameState);
            starImageResource = endGameStatePresenter.getStarImageResourceId();
        }

        levelListItemView.setData(levelTitle, levelNumber, levelHighScore, levelHighScoreLabel, starImageResource);

        return levelListItemView;
    }
}
