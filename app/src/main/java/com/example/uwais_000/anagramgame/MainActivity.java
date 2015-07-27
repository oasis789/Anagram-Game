package com.example.uwais_000.anagramgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uwais_000.anagramgame.view.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class MainActivity extends Activity {


    DraggableGridView dgv;
    char[] anagramSeed = "eee aaa polkui".toCharArray();
    ArrayList<String> sentence = new ArrayList<String>();
    String TAG = "MainActivity";
    int players, turnTime, rounds;
    TextView tvActivePlayer, tvTimeLeft, tvRound;
    int activePlayer, roundNumber;
    CountDownTimer timer;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        dgv = (DraggableGridView) findViewById(R.id.gridView);
        dgv.setColCount(anagramSeed.length);

        Intent intent = getIntent();
        players = intent.getIntExtra("PLAYERS", 2);
        turnTime = intent.getIntExtra("TIME", 30);
        rounds = intent.getIntExtra("ROUNDS", 3);

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
                Log.v(TAG, "End of turn: " + sentence);
                activePlayer++;

                // If new round
                if(activePlayer > players){
                    activePlayer = 1;
                    roundNumber ++;
                }

                if(roundNumber > rounds){
                    //Finish the game
                    Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_SHORT).show();
                    AlertDialog finishDialog = new AlertDialog.Builder(context)
                            .setMessage("The game is now finished!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
