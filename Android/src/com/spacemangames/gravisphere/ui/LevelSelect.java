package com.spacemangames.gravisphere.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.spacemangames.gravisphere.LevelDbAdapter;
import com.spacemangames.gravisphere.LevelListAdapter;
import com.spacemangames.gravisphere.R;
import com.spacemangames.gravisphere.contentprovider.HighScoresContentProvider;

@EActivity(R.layout.levelselect_layout)
public class LevelSelect extends ListActivity {
    private final class LevelSelectItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View aView, int position, long id) {
            int lID = Integer.parseInt((String) ((TextView) aView.findViewById(R.id.levelNumber)).getText());

            if (!dbHelper.levelIsUnlocked(lID)) {
                Toast.makeText(getApplicationContext(), R.string.level_locked, Toast.LENGTH_SHORT).show();
            } else {
                Intent aIntent = new Intent();
                aIntent.putExtra(SpaceApp.LEVEL_ID_STRING, lID);
                setResult(Activity.RESULT_OK, aIntent);
                finish();
            }
        }
    }

    @Bean
    protected LevelDbAdapter          dbHelper;
    private Cursor                    levelCursor;

    private final OnItemClickListener levelSelectedHandler = new LevelSelectItemClickListener();

    @Override
    protected void onResume() {
        super.onResume();
        GoogleAnalyticsTracker.getInstance().trackPageView("/levelselect");
    };

    @AfterViews
    protected void afterViews() {
        ListView listView = (ListView) this.findViewById(android.R.id.list);
        listView.addHeaderView(new View(this), null, true);
        listView.addFooterView(new View(this), null, true);

        fillData();

        getListView().setOnItemClickListener(levelSelectedHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
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
        levelCursor = getContentResolver().query(HighScoresContentProvider.CONTENT_URI, null, null, null, null);

        startManagingCursor(levelCursor);

        LevelListAdapter levelListAdapter = new LevelListAdapter(this, levelCursor);

        setListAdapter(levelListAdapter);
    }
}
