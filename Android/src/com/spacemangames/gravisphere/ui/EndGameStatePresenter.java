package com.spacemangames.gravisphere.ui;

import com.spacemangames.framework.EndGameState;
import com.spacemangames.gravisphere.R;

public enum EndGameStatePresenter {
    NOT_ENDED(-1, -1, -1),
    WON_BRONZE(R.drawable.star_bronze, R.string.end_level_title_won, R.string.end_level_subtitle_won),
    WON_SILVER(R.drawable.star_silver, R.string.end_level_subtitle_won, R.string.end_level_subtitle_won),
    WON_GOLD(R.drawable.star_gold, R.string.end_level_subtitle_won, R.string.end_level_subtitle_won),
    LOST_DIE(R.drawable.star_enabled, R.string.end_level_title_lost_die, R.string.end_level_subtitle_lost_die),
    LOST_LOST(R.drawable.star_enabled, R.string.end_level_title_lost_lost, R.string.end_level_subtitle_lost_die);

    private int starImageResourceId;
    private int msgResourceId;
    private int titleResourceId;

    EndGameStatePresenter(int starImageResourceId, int titleResourceId, int msgResourceId) {
        this.starImageResourceId = starImageResourceId;
        this.titleResourceId = titleResourceId;
        this.msgResourceId = msgResourceId;
    }

    public static EndGameStatePresenter valueOfEndGameState(EndGameState endGameState) {
        return valueOf(endGameState.name());
    }

    public int getStarImageResourceId() {
        return starImageResourceId;
    }

    public int getMsgResourceId() {
        return msgResourceId;
    }

    public int getTitleResourceId() {
        return titleResourceId;
    }
}
