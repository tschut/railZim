package com.spacemangames.gravisphere;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spacemangames.framework.EndGameState;
import com.spacemangames.gravisphere.ui.EndGameStatePresenter;
import com.spacemangames.library.SpaceData;
import com.spacemangames.pal.PALManager;

public class LevelListAdapter extends BaseAdapter {
    private static final String  TAG            = "LevelListAdapter";

    private static final int     TYPE_LEVEL     = 0;
    private static final int     TYPE_AD        = 1;
    private static final int     TYPE_MAX_COUNT = TYPE_AD + 1;

    private final LayoutInflater inflater;
    private final Cursor         cursor;

    public LevelListAdapter(Cursor cursor, LayoutInflater inflater) {
        this.inflater = inflater;
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

        PALManager.getLog().i(TAG, "Level: " + levelNumber + " " + levelHighScore + " points");
        convertView = inflater.inflate(R.layout.levelselect_item, null);
        TextView levelTitleTextView = (TextView) convertView.findViewById(R.id.level_title);
        TextView levelNumberTextView = (TextView) convertView.findViewById(R.id.level_number);
        TextView levelHighScoreTextView = (TextView) convertView.findViewById(R.id.level_points);
        TextView levelHighScoreTitleView = (TextView) convertView.findViewById(R.id.level_points_text);
        ImageView starImageView = (ImageView) convertView.findViewById(R.id.star_image);

        levelTitleTextView.setText(levelTitle);
        levelNumberTextView.setText(levelNumber);
        levelHighScoreTextView.setText(levelHighScore);

        levelHighScoreTextView.setVisibility(View.VISIBLE);
        levelHighScoreTitleView.setVisibility(View.VISIBLE);
        if (!LevelDbAdapter.getInstance().levelIsUnlocked(Integer.parseInt(levelNumber))) {
            starImageView.setImageResource(R.drawable.star_disabled);
            levelHighScoreTextView.setVisibility(View.INVISIBLE);
            levelHighScoreTitleView.setVisibility(View.INVISIBLE);
        } else if (Integer.parseInt(levelHighScore) == 0) {
            starImageView.setImageResource(R.drawable.star_enabled);
        } else {
            EndGameState endGameState = SpaceData.getInstance().levelStarColor(Integer.parseInt(levelNumber),
                    Integer.parseInt(levelHighScore));
            EndGameStatePresenter endGameStatePresenter = EndGameStatePresenter.valueOfEndGameState(endGameState);
            starImageView.setImageResource(endGameStatePresenter.getStarImageResourceId());
        }

        return convertView;
    }
}
