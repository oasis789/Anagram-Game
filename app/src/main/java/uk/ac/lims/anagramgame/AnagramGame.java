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
    private String TAG = "AnagramGame";
    private int numberOfPlayers, turnTime, numberOfRounds;
    private TextView tvActivePlayer, tvTimeLeft, tvRound;
    private int currentPlayerNumber, currentRoundNumber;
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
        setupTimer();

        /**saveGameMetaData();
        createLayout(null);

        currentPlayerNumber = 1;
        currentRoundNumber = 1;*/
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

                if(currentPlayerNumber > numberOfPlayers){
                    ///New Round
                    currentPlayerNumber = 1;
                    currentRoundNumber++;
                    Toast.makeText(context, "Round " + currentRoundNumber, Toast.LENGTH_SHORT).show();
                    gameEventListener.onRoundFinished();
                }

                if(currentRoundNumber > numberOfRounds){
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
                    gameEventListener.nextPlayersTurn(currentPlayerNumber);
                }

            }
        };
    }

    public void nextPlayer(){
        currentPlayerNumber++;
    }

    public void playGame() {
        Log.d(TAG, "playGame");

        setTextOfHeaders();
        //Ready dialog for next player
        AlertDialog dialog2 = new AlertDialog.Builder(context)
                .setMessage("Player " + currentPlayerNumber + "! Are you ready?")
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

    public void saveGameStateData() {
        Log.d(TAG, "saveGameStateData");

        localGameState.add(getString(sentence));
        //Save current game state in the cloud
        GameStateData gameStateData = new GameStateData();
        gameStateData.setCurrentPlayer(currentPlayerNumber);
        gameStateData.setCurrentRound(currentRoundNumber);
        gameStateData.setGameMetaData(gameMetaData);
        gameStateData.setGameStateData(getString(sentence));
        gameStateData.saveInBackground();
    }

    public void saveGameMetaData() {
        Log.d(TAG, "saveGameMetaData");

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
        tvActivePlayer.setText("Player " + currentPlayerNumber);
        tvRound.setText("Round " + currentRoundNumber);
        tvTimeLeft.setText(turnTime + " Seconds");
    }

    public void createLayout(char[] turnSentence) {
        if(turnSentence == null){
            turnSentence = anagramSeed;
            currentPlayerNumber = 1;
            currentRoundNumber = 1;
        }
        Log.d(TAG, "createLayout: " + String.valueOf(turnSentence));

        for (char letter: turnSentence){
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

    public int getCurrentPlayerNumber(){return currentPlayerNumber;}

    public int getNumberOfPlayers(){return numberOfPlayers;}

    public int getNumberOfRounds(){return numberOfRounds;}

    public int getCurrentRoundNumber(){return currentRoundNumber;}

    public GameMetaData getGameMetaData(){return gameMetaData;}

    public ArrayList<String> getLocalGameState(){return localGameState;}

    public String getSentence(){return getString(sentence);}

    public void setOnGameEventListener(OnGameEventListener l){this.gameEventListener = l;}

    public void setGameMetaData(GameMetaData gameMetaData){this.gameMetaData = gameMetaData;}

    public void setAnagramSentence(ArrayList<String> sentence){this.sentence = sentence;}

    public void setCurrentPlayerNumber(int currentPlayerNumber){this.currentPlayerNumber = currentPlayerNumber;}

    public void setCurrentRoundNumber(int currentRoundNumber){this.currentRoundNumber = currentRoundNumber;}


}
