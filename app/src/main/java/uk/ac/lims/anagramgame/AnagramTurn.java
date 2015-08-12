package uk.ac.lims.anagramgame;

import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by uwais_000 on 07/08/2015.
 */
public class AnagramTurn {

    public String sentence = "";
    public int turnCounter;
    public GameMetaData gameMetaData;
    public int activePlayer, currentRound;
    public static final String TAG = "AnagramTurn";

    public AnagramTurn(){

    }

    public byte[] persist(){
        JSONObject retVal = new JSONObject();
        try{
            retVal.put("sentence", sentence);
            retVal.put("turnCounter", turnCounter);
            retVal.put("activePlayer", activePlayer);
            retVal.put("currentRound", currentRound);
            retVal.put("gameMetaDataID", gameMetaData.getObjectId());

        }catch (JSONException e){
            e.printStackTrace();
        }

        String st = retVal.toString();

        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    static public AnagramTurn unpersist(byte[] byteArray){
        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new AnagramTurn();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        final AnagramTurn retVal = new AnagramTurn();

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has("sentence")) {
                retVal.sentence = obj.getString("sentence");
            }
            if (obj.has("turnCounter")) {
                retVal.turnCounter = obj.getInt("turnCounter");
            }
            if (obj.has("activePlayer")) {
                retVal.activePlayer = obj.getInt("activePlayer");
            }
            if (obj.has("currentRound")) {
                retVal.currentRound = obj.getInt("currentRound");
            }
            if (obj.has("gameMetaDataID")) {
                String gameMetaDataId = obj.getString("gameMetaDataID");
                //Get Parse Object from server using ID
                ParseQuery<GameMetaData> query = ParseQuery.getQuery("GameMetaData");
                query.getInBackground(gameMetaDataId, new GetCallback<GameMetaData>() {
                    @Override
                    public void done(GameMetaData gameMetaData, ParseException e) {
                        if(e == null){
                            retVal.gameMetaData = gameMetaData;
                            Log.d(TAG, "GameMetaData successfully retrieved from Parse servers");
                            Log.d(TAG, "GameMetaData Id: " + retVal.gameMetaData.getObjectId());
                        }else{
                            e.printStackTrace();
                            Log.d(TAG, "Failed to retrieve GameMetaData from Parse servers");
                        }
                    }
                });
            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retVal;
    }


}
