package uk.ac.lims.anagramgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.lims.anagramgame.view.DraggableGridView;
import uk.ac.lims.anagramgame.view.OnRearrangeListener;

/**
 * Created by uwais_000 on 04/08/2015.
 */
public class AnagramGame {

    private DraggableGridView dgv;
    private char[] anagramSeed = "aaa b c d eeee f g iii l m nn oo rr ss tt u             ".toCharArray();
    private ArrayList<String> sentence = new ArrayList<String>();
    private ArrayList<String> localGameState = new ArrayList<String>();
    private String TAG = "LocalAnagramGameActivity";
    private int numberOfPlayers, turnTime, numberOfRounds;
    private TextView tvActivePlayer, tvTimeLeft, tvRound;
    private int activePlayer, roundNumber;
    private CountDownTimer timer;
    private Context context;
    private GameMetaData gameMetaData;
    private OnGameEventListener gameEventListener;

    public AnagramGame(Context context, DraggableGridView dgv, TextView tvActivePlayer, final TextView tvTimeLeft, TextView tvRound, int numberOfPlayers, int numberOfRounds, int turnTime){
        this.context = context;
        this.dgv = dgv;
        dgv.setColCount(anagramSeed.length / 4);

        this.tvActivePlayer = tvActivePlayer;
        this.tvTimeLeft = tvTimeLeft;
        this.tvRound = tvRound;

        this.numberOfPlayers = numberOfPlayers;
        this.numberOfRounds = numberOfRounds;
        this.turnTime = turnTime;

        saveGameMetaData();
        createLayout();
        setupTimer();

        activePlayer = 1;
        roundNumber = 1;
    }

    private void setupTimer() {
        final ColorStateList defaultTextColor = tvTimeLeft.getTextColors();
        final Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(turnTime*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeLeft.setText(millisUntilFinished / 1000 + " Seconds");
                if(millisUntilFinished / 1000 < 6) {
                    v.vibrate(250);
                    tvTimeLeft.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFinish() {
                tvTimeLeft.setText("0 Seconds");
                v.vibrate(500);
                tvTimeLeft.setTextColor(defaultTextColor);
                //Toast.makeText(getApplicationContext(), "Times Up!!", Toast.LENGTH_SHORT).show();
                Log.v(TAG, "End of turn: " + getString(sentence));
                saveGameStateData();

                gameEventListener.onTurnFinished();
                nextPlayer();

                if(activePlayer > numberOfPlayers){
                    ///New Round
                    activePlayer = 1;
                    roundNumber++;
                    Toast.makeText(context, "Round " + roundNumber, Toast.LENGTH_SHORT).show();
                    gameEventListener.onRoundFinished();
                }

                if(roundNumber > numberOfRounds){
                    //Finish the game
                    Toast.makeText(context, "Finished!", Toast.LENGTH_SHORT).show();
                    gameMetaData.setGameFinishedState(true);
                    gameMetaData.saveInBackground();

                    for(String s:localGameState){
                        Log.v(TAG, "Game State " + s);
                    }

                    gameEventListener.onGameFinished();
                }else{
                    //Next Players Turn
                    gameEventListener.nextPlayersTurn(activePlayer);
                }

            }
        };
    }

    public void nextPlayer(){
        activePlayer++;
    }

    public void playGame() {
        setTextOfHeaders();
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

    private void saveGameStateData() {
        localGameState.add(getString(sentence));
        //Save current game state in the cloud
        GameStateData gameStateData = new GameStateData();
        gameStateData.setCurrentPlayer(activePlayer);
        gameStateData.setCurrentRound(roundNumber);
        gameStateData.setGameMetaData(gameMetaData);
        gameStateData.setGameStateData(getString(sentence));
        gameStateData.saveInBackground();
    }

    private void saveGameMetaData() {
        //Save Game meta-data to cloud
        gameMetaData = new GameMetaData();
        gameMetaData.setNumberOfPlayers(numberOfPlayers);
        gameMetaData.setStringSeed(String.valueOf(anagramSeed));
        gameMetaData.setNumberOfRounds(numberOfRounds);
        gameMetaData.setTurnTime(turnTime);
        gameMetaData.setGameFinishedState(false);
        gameMetaData.saveInBackground();
    }

    private void setTextOfHeaders(){
        tvActivePlayer.setText("Player " + activePlayer);
        tvRound.setText("Round " + roundNumber);
        tvTimeLeft.setText(turnTime + " Seconds");
    }

    private void createLayout() {
        for (char letter: anagramSeed){
            TextView view = new TextView(context);

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
    }

    private String getString(ArrayList<String> sentence){
        String s = sentence.toString();
        //Remove brackets from start and end of string
        s = s.substring(1,s.length() - 1);
        //Remove commas in the string
        s = s.replaceAll(",", "");
        s = s.trim();
        return s;
    }

    public int getActivePlayer(){return activePlayer;}

    public int getNumberOfPlayers(){return numberOfPlayers;}

    public int getNumberOfRounds(){return numberOfRounds;}

    public int getRoundNumber(){return roundNumber;}

    public ArrayList<String> getLocalGameState(){return localGameState;}

    public void setOnGameEventListener(OnGameEventListener l){this.gameEventListener = l;}


}
