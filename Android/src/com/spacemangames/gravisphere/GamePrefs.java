package com.spacemangames.gravisphere;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface GamePrefs {
    @DefaultBoolean(false)
    boolean hasSeenHelp();
}
