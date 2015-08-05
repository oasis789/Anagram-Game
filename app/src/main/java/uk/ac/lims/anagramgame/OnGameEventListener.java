package uk.ac.lims.anagramgame;

/**
 * Created by uwais_000 on 05/08/2015.
 */
public interface OnGameEventListener {

    public abstract void onTurnFinished();

    public abstract void onRoundFinished();

    public abstract void onGameFinished();

    public abstract void nextPlayersTurn(int activePlayer);
}
