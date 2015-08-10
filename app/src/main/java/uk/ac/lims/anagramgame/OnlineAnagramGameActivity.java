package uk.ac.lims.anagramgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uwais_000.anagramgame.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

import uk.ac.lims.anagramgame.view.DraggableGridView;


public class OnlineAnagramGameActivity extends Activity implements OnGameEventListener, GoogleApiClient.ConnectionCallbacks {

    private Context context;
    private TextView tvActivePlayer, tvTimeLeft, tvRound;
    private DraggableGridView dgv;
    private GoogleApiClient mGoogleApiClient;
    private int numberOfPlayers, numberOfRounds, turnTime;
    private ArrayList<String> invitees;
    private AnagramGame anagramGame;
    private AlertDialog mAlertDialog;
    public TurnBasedMatch mMatch;
    public AnagramTurn mTurnData;

    // Should I be showing the turn API?
    public boolean isDoingTurn = false;

    // Current turn-based match
    private TurnBasedMatch mTurnBasedMatch;

    public static final String TAG = "OnlineAnagramGame";
    private String nextParticipantId;

    final static int RC_SELECT_PLAYERS = 10000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        Intent intent = getIntent();
        numberOfRounds = intent.getIntExtra(GameMetaData.NUMBER_OF_ROUNDS_KEY, 3);
        turnTime = intent.getIntExtra(GameMetaData.TURN_TIME_KEY, 30);

        context = this;
        tvActivePlayer = (TextView) findViewById(R.id.tvActivePlayer);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        tvRound = (TextView) findViewById(R.id.tvRound);
        dgv = (DraggableGridView) findViewById(R.id.gridView);
        dgv.setColCount(14);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            numberOfPlayers = invitees.size() + 1;

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .build();

            // Create and start the match.
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc)
                    .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                            processResult(initiateMatchResult);
                        }
                    });

            showSpinner();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): Connecting to Google APIs");
        mGoogleApiClient.connect();
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): Disconnecting from Google APIs");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }*/


    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result){
        Log.d(TAG, "processResult - InitiateMatch Result");

        // Check if the status code is not success.
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }

        startMatch(match);
    }

    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        Log.d(TAG, "processResult - UpdateMatch Result");

        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        //if (match.canRematch()) {
        //    askForRematch();
        //}

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    //Called when match is first started!!!
    public void startMatch(TurnBasedMatch match) {
        Log.d(TAG, "startMatch - TurnBasedMatch");
        anagramGame = new AnagramGame(context, dgv, tvActivePlayer, tvTimeLeft, tvRound, numberOfPlayers, numberOfRounds, turnTime);
        anagramGame.setOnGameEventListener(this);


        mTurnData = new AnagramTurn();
        mTurnData.activePlayer = 1;
        mTurnData.currentRound = 1;

        // Some basic turn data
        anagramGame.createLayout(null);
        anagramGame.saveGameMetaData();
        mTurnData.gameMetaData = anagramGame.getGameMetaData();

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        showSpinner();

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match) {
        Log.d(TAG, "updateMatch - TurnBasedMatch");

        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    showWarning(
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                showWarning("Complete!",
                        "This game is over; someone finished it!  You can only finish it now.");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                Log.d(TAG, "updateMatch - My Turn");
                //Take my turn
                mTurnData = AnagramTurn.unpersist(mMatch.getData());
                setGameplayUI();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;
    }

    private void setGameplayUI() {
        Log.d(TAG, "setGameplayUI");
        isDoingTurn = true;
        anagramGame.createLayout(mTurnData.sentence.toCharArray());
        anagramGame.playGame();
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
                                 int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }


    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        this,
                        "Stored action for later.  (Please remove this toast before release.)",
                        Toast.LENGTH_SHORT).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            /**case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);*/
        }

        return false;
    }

    public void showSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.GONE);
    }

    @Override
    public void onTurnFinished() {
    }

    @Override
    public void onRoundFinished() {

    }

    @Override
    public void onGameFinished() {
        //TODO: Call finish on the match

    }

    @Override
    public void nextPlayersTurn(int activePlayer) {
        Log.d(TAG, "AnagramGame Listener - NextPlayersTurn");
        isDoingTurn = false;

        //Persist turn data to the cloud
        showSpinner();

        String nextParticipantId = getNextParticipantId();
        mTurnData.turnCounter += 1;
        mTurnData.activePlayer = anagramGame.getCurrentPlayerNumber();
        mTurnData.currentRound = anagramGame.getCurrentRoundNumber();
        mTurnData.sentence = anagramGame.getSentence();
        mTurnData.gameMetaData = anagramGame.getGameMetaData();

        //Call take turn and determine Id of next player

        showSpinner();
        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
        mTurnData.persist(), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                        processResult(updateMatchResult);
                    }
                });
        mTurnData = null;
    }

    public String getNextParticipantId() {
        Log.d(TAG, "getNextParticipantId");

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);
        Log.d(TAG, "My participant Id " + myParticipantId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();
        Log.d(TAG, "list of participants");
        for(String id: participantIds){
            Log.d(TAG, id);
        }

        Log.d(TAG, "Next players id: " + participantIds.get(anagramGame.getCurrentPlayerNumber() - 1));

        return participantIds.get(anagramGame.getCurrentPlayerNumber() - 1);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): Connection successful");

        // Retrieve the TurnBasedMatch from the bundle
        if (bundle != null) {
            mTurnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (mTurnBasedMatch != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(mTurnBasedMatch);
                return;
            }
        }else{
            Intent inviteIntent =
                    Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 4, false);
            startActivityForResult(inviteIntent, RC_SELECT_PLAYERS);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
        mGoogleApiClient.connect();
    }
}
