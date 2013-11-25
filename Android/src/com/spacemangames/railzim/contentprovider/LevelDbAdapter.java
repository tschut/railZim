package com.spacemangames.railzim.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.api.Scope;
import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.DebugSettings;

@EBean(scope = Scope.Singleton)
public class LevelDbAdapter {
    public static final String  KEY_ROWID        = "_id";
    public static final String  KEY_TITLE        = "title";
    public static final String  KEY_LEVELNUMBER  = "levelnumber";
    public static final String  KEY_HIGHSCORE    = "highscore";

    private static final String DATABASE_NAME    = "data";
    private static final String DATABASE_TABLE   = "leveldata";
    private static final int    DATABASE_VERSION = 1;

    private static final String TAG              = "LevelDbAdapter";

    private static boolean      mJustCreated     = false;

    /** Database creation SQL statement */
    private static final String DATABASE_CREATE  = "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement, "
                                                         + KEY_TITLE + " text not null, " + KEY_LEVELNUMBER + " integer not null, "
                                                         + KEY_HIGHSCORE + " integer not null);";

    private DatabaseHelper      mDbHelper;
    private SQLiteDatabase      mDb;

    @RootContext
    protected Context           context;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            db.execSQL(DATABASE_CREATE);

            mJustCreated = true;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            PALManager.getLog().w(TAG,
                    "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    @AfterInject
    protected void open() {
        mDbHelper = new DatabaseHelper(context);
        mDb = mDbHelper.getWritableDatabase();

        // force onCreate if DEBUG
        if (DebugSettings.DEBUG_DB_CREATION) {
            mDbHelper.onCreate(mDb);
        }
    }

    public synchronized void insertAllLevelsIfEmpty() {
        if (mJustCreated) {
            insertAllLevels();
            mJustCreated = false;
        }
    }

    /** Check if we need to put in the level data and do so if necessary */
    private void insertAllLevels() {
        SpaceData lData = SpaceData.getInstance();

        for (SpaceLevel level : lData.levels) {
            insertLevel(level.name, level.id, 0);
        }
    }

    /**
     * Insert a level. Normally, this should only be done on the first run of
     * the app, for all levels in the levels xml file
     */
    private void insertLevel(String aTitle, int aLevelNr, int aHighScore) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_TITLE, aTitle);
        initialValues.put(KEY_LEVELNUMBER, aLevelNr);
        initialValues.put(KEY_HIGHSCORE, aHighScore);

        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /** Return a Cursor over the list of all levels in the database */
    public Cursor fetchAllLevels() {
        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_LEVELNUMBER, KEY_HIGHSCORE }, null, null, null, null,
                null);
    }

    public int highScore(long aLevelID) {
        Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_HIGHSCORE }, KEY_LEVELNUMBER + "=" + aLevelID, null, null, null, null);
        cursor.moveToFirst();
        // int lResult =
        // cursor.getInt(cursor.getColumnIndex(LevelDbAdapter.KEY_HIGHSCORE));
        cursor.close();

        return 0;
    }

    public boolean levelIsUnlocked(long aLevelID) {
        if (DebugSettings.DEBUG_ALL_LEVELS_UNLOCKED)
            return true;

        if (aLevelID == 0) // first level, always unlocked
            return true;

        // check if level exists
        Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_HIGHSCORE }, KEY_LEVELNUMBER + "=" + (aLevelID), null, null, null,
                null);

        if (cursor.getCount() == 0) { // level doesn't exist, so pretend it's
                                      // locked
            cursor.close();
            return false;
        }

        cursor.close();
        cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_HIGHSCORE }, KEY_LEVELNUMBER + "=" + (aLevelID - 1), null, null, null, null);

        cursor.moveToFirst();
        int lResult = cursor.getInt(cursor.getColumnIndex(LevelDbAdapter.KEY_HIGHSCORE));
        cursor.close();

        return lResult > 0;
    }

    public int getLastUnlockedLevelID() {
        Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_HIGHSCORE }, null, null, null, null, null);

        int count = cursor.getCount();
        int i = 0;
        for (; i < count; ++i) {
            cursor.moveToPosition(i);
            int score = cursor.getInt(cursor.getColumnIndex(LevelDbAdapter.KEY_HIGHSCORE));
            if (score == 0) // first level that wasn't finished
                break;
        }
        cursor.close();

        if (i != count)
            return i;
        else
            return 0; // all levels completed, start again at the first one
    }

    public boolean updateHighScore(long aLevelID, long aNewScore) {
        ContentValues args = new ContentValues();
        args.put(KEY_HIGHSCORE, aNewScore);

        return mDb.update(DATABASE_TABLE, args, KEY_LEVELNUMBER + "=" + aLevelID, null) > 0;
    }
}
