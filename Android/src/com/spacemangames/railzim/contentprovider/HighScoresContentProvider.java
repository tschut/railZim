package com.spacemangames.railzim.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EProvider;

@EProvider
public class HighScoresContentProvider extends ContentProvider {
    public static Uri        CONTENT_URI = Uri.parse("content://com.spacemangames.gravisphere/highscore");

    @Bean
    protected LevelDbAdapter dbHelper;

    public HighScoresContentProvider() {
        super();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        dbHelper.insertAllLevelsIfEmpty();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return dbHelper.fetchAllLevels();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        dbHelper.updateHighScore(Integer.parseInt(selection), values.getAsInteger("score"));
        return 1;
    }
}
