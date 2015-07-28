package com.example.uwais_000.anagramgame;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by uwais_000 on 28/07/2015.
 */
public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(GameMetaData.class);
        ParseObject.registerSubclass(GameStateData.class);
        Parse.initialize(this, "whDhW7uOkCmRc2frgAARWIOeu2J1mhHqERjlnslB", "tV5SByI0DR0BqvXWxyJTOO7D4ibYu11stDGj3cxg");

    }
}
