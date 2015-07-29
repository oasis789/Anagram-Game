package com.example.uwais_000.anagramgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class GameResultsActivity extends ActionBarActivity {

    private TableLayout table;
    private ArrayList<String> localGameState;
    private int numberOfPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);
        table = (TableLayout) findViewById(R.id.tableLayout);
        Intent intent = getIntent();
        localGameState = intent.getStringArrayListExtra(GameStateData.GAME_STATE_DATA_KEY);
        numberOfPlayers = intent.getIntExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, 2);

        TextView tvOriginalString = (TextView)  findViewById(R.id.tvOriginalString);
        tvOriginalString.setText(localGameState.get(0));
        localGameState.remove(0);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int currentPlayer = 1;

        for(String s:localGameState){
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.game_result_table_row, null);
            TextView tvPlayer = (TextView) tableRow.findViewById(R.id.tvResultPlayerNum);
            TextView tvString = (TextView) tableRow.findViewById(R.id.tvResultString);
            tvPlayer.setText(String.valueOf(currentPlayer));
            tvString.setText(s);

            table.addView(tableRow);

            currentPlayer++;
            if(currentPlayer > numberOfPlayers){
                currentPlayer = 1;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_results, menu);
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
