package com.example.uwais_000.anagramgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uwais_000.anagramgame.view.*;

import java.util.ArrayList;


public class MainActivity extends Activity {


    DraggableGridView dgv;
    char[] anagramSeed = "aaa b c d eeee f g iii l m nn oo rr ss tt u             ".toCharArray();
    ArrayList<String> sentence = new ArrayList<String>();
    ArrayList<String> localGameState = new ArrayList<String>();
    String TAG = "MainActivity";
    int numberOfPlayers, turnTime, numberOfRounds;
    TextView tvActivePlayer, tvTimeLeft, tvRound;
    int activePlayer, roundNumber;
    CountDownTimer timer;
    Context context;
    GameMetaData gameMetaData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        dgv = (DraggableGridView) findViewById(R.id.gridView);
        Log.v(TAG, String.valueOf(anagramSeed.length/4));
        dgv.setColCount(anagramSeed.length / 4);

        Intent intent = getIntent();
        numberOfPlayers = intent.getIntExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, 2);
        turnTime = intent.getIntExtra(GameMetaData.TURN_TIME_KEY, 30);
        numberOfRounds = intent.getIntExtra(GameMetaData.NUMBER_OF_ROUNDS_KEY, 3);

        //Save Game meta-data to cloud
        gameMetaData = new GameMetaData();
        gameMetaData.setNumberOfPlayers(numberOfPlayers);
        gameMetaData.setStringSeed(String.valueOf(anagramSeed));
        gameMetaData.setNumberOfRounds(numberOfRounds);
        gameMetaData.setTurnTime(turnTime);
        gameMetaData.setGameFinishedState(false);
        gameMetaData.saveInBackground();

        createLayout();

        timer = new CountDownTimer(turnTime*1000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeLeft.setText(millisUntilFinished / 1000 + " Seconds");
            }

            @Override
            public void onFinish() {
                tvTimeLeft.setText("0 Seconds");
                Toast.makeText(getApplicationContext(), "Times Up!!", Toast.LENGTH_SHORT).show();
                Log.v(TAG, "End of turn: " + getString(sentence));

                localGameState.add(getString(sentence));
                //Save current game state in the cloud
                GameStateData gameStateData = new GameStateData();
                gameStateData.setCurrentPlayer(activePlayer);
                gameStateData.setCurrentRound(roundNumber);
                gameStateData.setGameMetaData(gameMetaData);
                gameStateData.setGameStateData(getString(sentence));
                gameStateData.saveInBackground();

                activePlayer++;

                // If new round
                if(activePlayer > numberOfPlayers){
                    activePlayer = 1;
                    roundNumber ++;
                }

                if(roundNumber > numberOfRounds){
                    //Finish the game
                    Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_SHORT).show();
                    gameMetaData.setGameFinishedState(true);
                    gameMetaData.saveInBackground();

                    for(String s:localGameState){
                        Log.v(TAG, "Game State " + s);
                    }

                    AlertDialog finishDialog = new AlertDialog.Builder(context)
                            .setMessage("The game is now finished!")
                            .setPositiveButton("View Game Summary", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Start Game Summary Activity
                                    Intent i = new Intent(getApplicationContext(), GameResultsActivity.class);
                                    i.putStringArrayListExtra(GameStateData.GAME_STATE_DATA_KEY, localGameState);
                                    i.putExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, numberOfPlayers);
                                    startActivity(i);
                                    finish();
                                }
                            }).setCancelable(false).create();
                    finishDialog.show();
                    timer.cancel();


                }else{
                    //Dialog asking user to pass device
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setMessage("Your turn has now finished. Please pass the device to Player " + activePlayer)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    tvActivePlayer.setText("Player " + activePlayer);
                                    if (activePlayer == 1){
                                        Toast.makeText(getApplicationContext(), "Round " + roundNumber, Toast.LENGTH_SHORT).show();
                                        tvRound.setText("Round " + roundNumber);
                                    }
                                    tvTimeLeft.setText(turnTime + " Seconds");
                                    //Play
                                    playGame();
                                }
                            }).setCancelable(false).create();
                    dialog.show();
                }
            }
        };

        activePlayer = 1;
        tvActivePlayer.setText("Player " + activePlayer);
        //First Round is round one
        roundNumber = 1;
        tvRound.setText("Round " + roundNumber);
        tvTimeLeft.setText(turnTime + " Seconds");
        playGame();
    }

    private String getString(ArrayList<String> sentence){
        String s = sentence.toString();
        //Remove brackets from start and end of string
        s = s.substring(1,s.length() - 1);
        //Remove commas in the string
        s = s.replaceAll(",", "");
        return s;
    }

    private void createLayout() {
        for (char letter: anagramSeed){
            TextView view = new TextView(MainActivity.this);

            view.setText(String.valueOf(letter));
            view.setBackgroundColor(Color.BLUE);
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            view.setAllCaps(true);
            view.setTextSize(25);
            view.setPadding(10, 10, 10, 10);

            dgv.addView(view);
            sentence.add(String.valueOf(letter));
        }

        localGameState.add(getString(sentence));

        dgv.setOnRearrangeListener(new OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
                String word = sentence.remove(oldIndex);
                if (oldIndex < newIndex)
                    sentence.add(newIndex, word);
                else
                    sentence.add(newIndex, word);
            }
        });

        tvActivePlayer = (TextView) findViewById(R.id.tvActivePlayer);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        tvRound = (TextView) findViewById(R.id.tvRound);
    }

    private void playGame() {
        //Ready dialog for next player
        AlertDialog dialog2 = new AlertDialog.Builder(context)
                .setMessage("Player " + activePlayer + "! Are you ready?")
                .setPositiveButton("Let's Play!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //Play
                        timer.start();
                    }
                }).setCancelable(false).create();
        dialog2.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
