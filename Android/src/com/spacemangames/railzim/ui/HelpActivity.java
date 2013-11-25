package com.spacemangames.railzim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.spacemangames.railzim.GamePrefs_;
import com.spacemangames.railzim.R;

@EActivity(R.layout.help_layout)
public class HelpActivity extends Activity {
    @Pref
    protected GamePrefs_ gamePrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            gamePrefs.hasSeenHelp().put(true);
        } else {
            // we are being restored: restart the app
            Intent i = new Intent(this, LoadingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        }
    }
}
