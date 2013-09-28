package com.spacemangames.gravisphere;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.spacemangames.gravisphere.R;
import com.spacemangames.pal.PALManager;

public class LevelSelect extends ListActivity {
    private static final String       TAG                   = "LevelSelect";

    private LevelDbAdapter            mDbHelper;
    private Cursor                    mLevelCursor;

    // Create a message handling object as an anonymous class.
    private final OnItemClickListener mLevelSelectedHandler = new OnItemClickListener() {
                                                                public void onItemClick(AdapterView<?> parent, View aView, int position,
                                                                        long id) {
                                                                    // return
                                                                    // the
                                                                    // result
                                                                    int lID = Integer.parseInt((String) ((TextView) aView
                                                                            .findViewById(R.id.level_number)).getText());

                                                                    if (!LevelDbAdapter.getInstance().levelIsUnlocked(lID)) {
                                                                        Toast.makeText(getApplicationContext(), R.string.level_locked,
                                                                                Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Intent aIntent = new Intent();
                                                                        aIntent.putExtra(SpaceApp.LEVEL_ID_STRING, lID);
                                                                        setResult(Activity.RESULT_OK, aIntent);
                                                                        finish();
                                                                    }
                                                                }
                                                            };

    @Override
    protected void onResume() {
        super.onResume();
        GoogleAnalyticsTracker.getInstance().trackPageView("/levelselect");
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            PALManager.getLog().i(TAG, "Normal startup");

            setContentView(R.layout.levelselect_layout);

            mDbHelper = LevelDbAdapter.getInstance();

            fillData();

            getListView().setOnItemClickListener(mLevelSelectedHandler);
        } else {
            // we are being restored: restart the app
            Intent i = new Intent(getApplicationContext(), LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fillData() {
        mLevelCursor = mDbHelper.fetchAllLevels();
        startManagingCursor(mLevelCursor);

        LevelListAdapter lLevelListAdapter = new LevelListAdapter(mLevelCursor,
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        setListAdapter(lLevelListAdapter);
    }
}
