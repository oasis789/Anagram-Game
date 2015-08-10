package uk.ac.lims.anagramgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uwais_000.anagramgame.R;

import uk.ac.lims.anagramgame.view.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class LocalAnagramGameActivity extends Activity implements OnGameEventListener {


    private DraggableGridView dgv;
    private String TAG = "LocalAnagramGameActivity";
    private int numberOfPlayers, turnTime, numberOfRounds;
    private TextView tvActivePlayer, tvTimeLeft, tvRound;
    private Context context;
    private AnagramGame anagramGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        tvActivePlayer = (TextView) findViewById(R.id.tvActivePlayer);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        tvRound = (TextView) findViewById(R.id.tvRound);
        dgv = (DraggableGridView) findViewById(R.id.gridView);

        Intent intent = getIntent();
        numberOfPlayers = intent.getIntExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, 2);
        turnTime = intent.getIntExtra(GameMetaData.TURN_TIME_KEY, 30);
        turnTime = 5;
        numberOfRounds = intent.getIntExtra(GameMetaData.NUMBER_OF_ROUNDS_KEY, 3);

        anagramGame = new AnagramGame(this, dgv, tvActivePlayer, tvTimeLeft, tvRound, numberOfPlayers, numberOfRounds, turnTime);
        anagramGame.setOnGameEventListener(this);
        anagramGame.createLayout(null);
        anagramGame.saveGameMetaData();
        anagramGame.playGame();
    }

    private void saveToFile(String objectId, String fileContents){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Anagram Game/" + objectId);
        myDir.mkdirs();
        String fileName = objectId + ".txt";
        File file = new File(myDir, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(fileContents + "\r\n");
            osw.flush();
            osw.close();
        }catch (IOException e){
            e.printStackTrace();
        }

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

    @Override
    public void onTurnFinished() {
    }

    @Override
    public void onRoundFinished() {
    }

    @Override
    public void onGameFinished() {
        AlertDialog finishDialog = new AlertDialog.Builder(context)
                .setMessage("The game is now finished!")
                .setPositiveButton("View Game Summary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Start Game Summary Activity
                        Intent i = new Intent(getApplicationContext(), GameResultsActivity.class);
                        i.putStringArrayListExtra(GameStateData.GAME_STATE_DATA_KEY, anagramGame.getLocalGameState());
                        i.putExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, anagramGame.getNumberOfPlayers());
                        startActivity(i);
                        finish();
                    }
                }).setCancelable(false).create();
        finishDialog.show();
    }

    @Override
    public void nextPlayersTurn(int activePlayer) {
        //If in multiplayer mode, otherwise continue
        if(numberOfPlayers > 1){
            //Dialog asking user to pass device
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("Your turn has now finished. Please pass the device to Player " + activePlayer)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            anagramGame.playGame();
                        }
                    }).setCancelable(false).create();
            dialog.show();
        } else{
            //Play
            anagramGame.playGame();
        }

    }
}
