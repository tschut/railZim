package com.spacemangames.railzim;

public class DebugSettings {
    // disable all debug stuff
    public static final boolean DEBUG                     = false;

    // setting this to false disables all logging that uses
    // com.games.spaceman.Log
    public static final boolean DEBUG_LOGGING             = true & DEBUG;

    // setting this to true will create the database on every start of the app
    public static final boolean DEBUG_DB_CREATION         = false & DEBUG;

    // Unlock all levels
    public static final boolean DEBUG_ALL_LEVELS_UNLOCKED = false & DEBUG;
}
