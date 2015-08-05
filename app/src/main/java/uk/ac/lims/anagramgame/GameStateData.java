package uk.ac.lims.anagramgame;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by uwais_000 on 28/07/2015.
 */

@ParseClassName("GameStateData")
public class GameStateData extends ParseObject {

    public static final String GAME_META_DATA_KEY = "gameMetaData";
    public static final String CURRENT_PLAYER_KEY = "currentPlayer";
    public static final String CURRENT_ROUND_KEY = "currentRound";
    public static final String GAME_STATE_DATA_KEY = "gameStateData";

    public GameStateData(){

    }

    //Getters
    public GameMetaData getGameMetaData(){
        return (GameMetaData) getParseObject(GAME_META_DATA_KEY);
    }

    public int getCurrentPlayer(){
        return getInt(CURRENT_PLAYER_KEY);
    }

    public int getCurrentRound(){
        return getInt(CURRENT_ROUND_KEY);
    }

    public String getGameStateData(){
        return getString(GAME_STATE_DATA_KEY);
    }

    //Setters
    public void setGameMetaData(GameMetaData gameMetaData){
        put(GAME_META_DATA_KEY, gameMetaData);
    }

    public void setCurrentPlayer(int currentPlayer){
        put(CURRENT_PLAYER_KEY, currentPlayer);
    }

    public void setCurrentRound(int currentRound){
        put(CURRENT_ROUND_KEY, currentRound);
    }

    public void setGameStateData(String gameStateData){
        put(GAME_STATE_DATA_KEY, gameStateData);
    }

}
