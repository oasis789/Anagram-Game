package com.example.uwais_000.anagramgame;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainMenuActivity extends ActionBarActivity implements View.OnClickListener{

    private Button singlePlayer, localMultiplayer, onlineMultiplayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        singlePlayer = (Button) findViewById(R.id.btnSinglePlayer);
        singlePlayer.setOnClickListener(this);
        localMultiplayer = (Button) findViewById(R.id.btnLocalMultiplayer);
        localMultiplayer.setOnClickListener(this);
        onlineMultiplayer = (Button) findViewById(R.id.btnOnlineMultiplayer);
        onlineMultiplayer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnSinglePlayer:
                Intent singlePlayerIntent = new Intent(this, MainActivity.class);
                singlePlayerIntent.putExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, 1);
                singlePlayerIntent.putExtra(GameMetaData.TURN_TIME_KEY, 3*60);
                singlePlayerIntent.putExtra(GameMetaData.NUMBER_OF_ROUNDS_KEY, 1);
                startActivity(singlePlayerIntent);
                break;

            case R.id.btnLocalMultiplayer:
                Intent localMultiplayerIntent = new Intent(this, StartActivity.class);
                startActivity(localMultiplayerIntent);
                break;

            case R.id.btnOnlineMultiplayer:
                Toast.makeText(getApplicationContext(), "Online Multiplayer",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
