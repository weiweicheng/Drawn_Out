package com.example.Drawn_Out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import com.example.Drawn_Out.entities.Game;
import com.example.Drawn_Out.entities.Phase;
import com.parse.*;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateGameActivity extends Activity {
    private String username;
    private String gameId;
    private Button startGameButton;
    private TextView numOfPlayersWaiting;
    private TextView minPlayerMessage;
    private TextView gameIdMessage;
    private Game localGame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
    }
        setContentView(R.layout.create_game);

        String gameId = generateGameId();
        localGame = new Game(gameId, username);
        saveGameToCloud(localGame);
        HashMap<String,String> params = new HashMap<>();
        params.put("Id",gameId);
        try {
            ParseCloud.callFunction("updateCurrentWord", params);
            gameIdMessage = (TextView) findViewById(R.id.gameIdMessage);
            gameIdMessage.setText("Your game id is " + gameId);
            refreshCurrentPlayers(localGame.getId());
        } catch (ParseException e) {
            Log.e("CreateGameActivity", e.getMessage());
        }
    }

    private void saveGameToCloud(Game localGame) {
        ParseObject newGame = new ParseObject("Game");;
        newGame.put("Id", localGame.getId());
        newGame.put("NumOfPlayersWaiting", localGame.getPlayersWaiting());
        newGame.put("CurrentArtist", localGame.getCurrentArtist());
        newGame.put("GamePhase", localGame.getGamePhase().toString());
        newGame.put("Scores", localGame.getScores());
        newGame.put("Players", localGame.getPlayers());
        try {
            newGame.save();
        } catch (ParseException e) {
            Log.e("CreateGameActivity", "Error when saving game to cloud " + e.getMessage());
        }

    }

    private String generateGameId() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        List<String> idList = new ArrayList<>() ;
        List<ParseObject> gameList = new ArrayList<>();
        try {
            gameList = query.find();
            for (ParseObject game : gameList) {
                idList.add(game.getString("Id"));
            }
            int attempts = 0;
            do {
                attempts++;
                if (attempts == 10) {
                    this.finish();
                }
                gameId = createId();
                Log.i("CreateGameActivity", "Generated gameId: " + gameId);
            } while (idList.contains(gameId));
            return gameId;
        } catch (ParseException e) {
            Log.e("CreateMenuActivity", e.getMessage());
        }
        return null;
    }

    private void refreshCurrentPlayers(String gameId) throws ParseException {

        numOfPlayersWaiting = (TextView) findViewById(R.id.numOfPlayers);
        startGameButton = (Button) findViewById(R.id.startGameButton);
        minPlayerMessage = (TextView) findViewById(R.id.minPlayerMessage);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
                    query.whereEqualTo("Id",gameId);
                    List<ParseObject> gameList = query.find();
                    List<String> playerList = gameList.get(0).getList("Players");
                    numOfPlayersWaiting.setText("Current number of players waiting: " + (playerList.size()));
                    if (playerList.size() >= 3) {
                        startGameButton.setVisibility(View.VISIBLE);
                        startGameButton.setClickable(true);
                        minPlayerMessage.setVisibility(View.INVISIBLE);
                    } else {
                        startGameButton.setVisibility(View.INVISIBLE);
                        startGameButton.setClickable(false);
                        minPlayerMessage.setVisibility(View.VISIBLE);
                    }
                } catch (ParseException e) {
                    Log.e("CreateGameActivity", e.getMessage());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, InGameActivity.class);
        intent.putExtra("gameId", localGame.getId());
        intent.putExtra("username", username);

        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
            query.whereEqualTo("Id", localGame.getId());
            ParseObject game = query.find().get(0);
            game.put("GamePhase", localGame.getGamePhase().toString());
            game.save();
        } catch (ParseException e) {
            Log.e("CreateGameActivity", e.getMessage());
        }
        startActivity(intent);
    }

    private String createId() {
        String ID = "";
        for (int i = 0; i < 4; i++) {
            ID += (char) (Math.random()*26 + 97);
        }
        return ID;
    }
}

