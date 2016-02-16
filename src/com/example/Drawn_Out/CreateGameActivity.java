package com.example.Drawn_Out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import com.example.Drawn_Out.entities.Phase;
import com.parse.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateGameActivity extends Activity {
    private String username;
    private String gameID;
    private List<String> idList = new ArrayList<>() ;
    private ParseObject newGame;
    private Button startGameButton;
    private TextView numOfPlayersWaiting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
    }
        setContentView(R.layout.create_game);
        Parse.initialize(this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        List<ParseObject> gameList = new ArrayList<>();

        //Grabs list of Id's from the cloud
        try {
            gameList = query.find();
        } catch (ParseException e) {
            Log.e("CreateMenuActivity", e.getMessage());
        }

        for (ParseObject game : gameList) {
            idList.add(game.getString("Id"));
        }

        //Checks if any current games in the cloud have matching IDs
        //TODO: Handle break condition to return back to menuActivity
        int attempts = 0;
        do {
            attempts++;
            if (attempts == 10) {
                this.finish();
            }
            gameID = createID();
            Log.i("CreateGameActivity", "Created game with game ID: " + gameID);
        } while (idList.contains(gameID));

        //Creates Game object on the cloud

        List<Integer> scoreList = new ArrayList<>();
        newGame = new ParseObject("Game");
        newGame.put("Id", gameID);
        newGame.put("NumOfPlayersWaiting", 0);
        newGame.put("CurrentArtist", username);
        newGame.put("GamePhase", Phase.PLAYERJOINING.toString());

        scoreList.add(0); //current player's score
        newGame.put("Scores", scoreList);

        List<String> nameList = new ArrayList<String>();
        nameList.add(username);
        newGame.put("Players", nameList);

        try {
            newGame.save();
        } catch (ParseException e) {
            Log.e("CreateGameActivity", e.getMessage());
        }
        HashMap<String,String> params = new HashMap<>();
        params.put("Id",gameID);
        try {
            ParseCloud.callFunction("updateCurrentWord",params);
        } catch (ParseException e) {
            Log.e("CreateGameActivity", e.getMessage());
        }

        //Polling the server
        numOfPlayersWaiting = (TextView) findViewById(R.id.numOfPlayers);
        startGameButton = (Button) findViewById(R.id.startGameButton);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    refreshCurrentPlayers();
                } catch (ParseException e) {
                    Log.e("CreateGameActivity", e.getMessage());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

    }

    public void refreshCurrentPlayers() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id",gameID);
        List<ParseObject> gameList = query.find();
        List<String> playerList = gameList.get(0).getList("Players");
        numOfPlayersWaiting.setText("Current number of players waiting: " + (playerList.size() - 1));
        if (playerList.size() >= 3) {
            startGameButton.setVisibility(View.VISIBLE);
            startGameButton.setClickable(true);
        } else {
            startGameButton.setVisibility(View.INVISIBLE);
            startGameButton.setClickable(false);
        }
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, InGameActivity.class);
        startActivity(intent);
    }

    public String createID() {
        double rn;
        String ID = "";
        char temp = 'a';
        for (int i = 0; i < 4; i++) {
            rn = Math.random() * 26 + 97;
            temp = (char)rn;
            ID += temp;
        }

        return ID;


    }
}

